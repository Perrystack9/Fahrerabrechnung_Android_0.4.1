#!/usr/bin/env python3
"""Pure synthetic proof of a bounded EF-reading proposal, not Android/card I/O.

Models the TCS43 offset/length statuses and explicitly accepted EOF variants.
6C00 at EOF remains unresolved and is rejected; no card command is sent.
"""
from dataclasses import dataclass
import json


class BoundaryError(Exception):
    pass


@dataclass(frozen=True)
class Response:
    data: bytes
    status: int


def complete_bounded_file(read, minimum=204, maximum=341):
    def exact(offset, length):
        result = bytearray()
        while len(result) < length:
            request = min(256, length - len(result))
            response = read(offset + len(result), request)
            if response.status != 0x9000 or not 1 <= len(response.data) <= request:
                raise BoundaryError('Incomplete or malformed data response')
            result.extend(response.data)
        return bytes(result)

    probes = 0

    def exists(offset):
        nonlocal probes
        probes += 1
        response = read(offset, 1)
        if response.status == 0x9000 and len(response.data) == 1:
            return True
        if not response.data and response.status in (0x6700, 0x6B00, 0x6282):
            return False
        raise BoundaryError('Unknown or malformed boundary response')

    prefix = exact(0, minimum)
    if exists(maximum):
        raise BoundaryError('File exceeds the specified maximum')
    low, high = minimum, maximum
    while low < high:
        midpoint = (low + high) // 2
        if exists(midpoint):
            low = midpoint + 1
        else:
            high = midpoint
    return prefix + exact(minimum, low - minimum), probes


class SyntheticFile:
    def __init__(self, raw, chunk=256, eof='TCS43', failure=None):
        self.raw, self.chunk, self.eof, self.failure = raw, chunk, eof, failure
        self.calls = []

    def read(self, offset, length):
        self.calls.append((offset, length))
        if offset < 0 or offset > 32767 or not 1 <= length <= 256:
            raise AssertionError('Read request outside the existing short-APDU bounds')
        if self.failure and offset >= 204:
            if self.failure == 'empty_success':
                return Response(b'', 0x9000)
            if self.failure == 'excess_data':
                return Response(bytes(length + 1), 0x9000)
            if self.failure == 'error_with_data':
                return Response(b'\x11', 0x6700)
            if self.failure == '6C00':
                return Response(b'', 0x6C00)
            if self.failure == 'timeout':
                raise BoundaryError('Synthetic timeout')
            if self.failure == 'disconnect':
                raise BoundaryError('Synthetic disconnect')
        if offset >= len(self.raw):
            if self.eof == 'LEGACY_6B00':
                return Response(b'', 0x6B00)
            if self.eof == 'ISO_WARNING':
                return Response(b'', 0x6282)
            return Response(b'', 0x6700 if offset == len(self.raw) else 0x6B00)
        if offset + length > len(self.raw):
            return Response(b'', 0x6700)
        return Response(self.raw[offset:offset + min(length, self.chunk)], 0x9000)


def main():
    accepted = 0
    max_probes = 0
    for length in range(204, 342):
        variants = (bytes((i * 17 + 3) % 256 for i in range(length)), bytes(length), bytes([255]) * length)
        for raw in variants:
            for chunk in (1, 7, 137, 256):
                for eof in ('TCS43', 'LEGACY_6B00', 'ISO_WARNING'):
                    file = SyntheticFile(raw, chunk, eof)
                    result, probes = complete_bounded_file(file.read)
                    assert result == raw and probes <= 9
                    max_probes = max(max_probes, probes)
                    accepted += 1
    rejected = 0
    invalid = [SyntheticFile(bytes(size)) for size in (0, 203, 342)]
    invalid += [SyntheticFile(bytes(205), failure=mode) for mode in
        ('empty_success', 'excess_data', 'error_with_data', '6C00', 'timeout', 'disconnect')]
    for file in invalid:
        try:
            complete_bounded_file(file.read)
        except BoundaryError:
            rejected += 1
        else:
            raise AssertionError('Malformed or out-of-range file was accepted')
    # A failed source does not supply bytes or replace the other complete sources.
    sources = {'MA': SyntheticFile(bytes(204)), 'SIGN': SyntheticFile(bytes(205)),
        'CA': SyntheticFile(bytes(337), failure='timeout'), 'LINK': SyntheticFile(bytes(341))}
    saved, failed = {}, []
    for name, file in sources.items():
        try:
            saved[name], _ = complete_bounded_file(file.read)
        except BoundaryError:
            failed.append(name)
    assert set(saved) == {'MA', 'SIGN', 'LINK'} and failed == ['CA']
    print(json.dumps({'synthetic_complete_files_preserved': accepted,
        'possible_lengths_checked': 138, 'maximum_one_byte_boundary_probes': max_probes,
        'malformed_or_out_of_range_files_rejected': rejected,
        'independent_source_failure_preserves_others': True,
        'android_implementation_or_hardware_validation': False}, indent=2))


if __name__ == '__main__':
    main()
