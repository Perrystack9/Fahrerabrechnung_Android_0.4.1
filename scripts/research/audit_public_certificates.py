#!/usr/bin/env python3
"""Offline format research on JRC's public v1.4 examples; never an app trust check.

Reads only .cert members. It neither extracts nor reads the example private keys.
No network, card commands, certificate contents or equipment identifiers are logged.
"""
import argparse
from collections import Counter
from dataclasses import dataclass
import hashlib
import json
from pathlib import Path
import zipfile

ARCHIVE_SHA256 = 'b8ed453eb99f2a889b888c02b17eed33ee1e5c9c8dd0a6c182ced74a39b0ae5c'
CURVES = {
    '2a8648ce3d030107': ('NIST P-256', 'SECP256R1', 65),
    '2b81040022': ('NIST P-384', 'SECP384R1', 97),
    '2b81040023': ('NIST P-521', 'SECP521R1', 133),
    '2b2403030208010107': ('Brainpool P256r1', 'BrainpoolP256R1', 65),
    '2b240303020801010b': ('Brainpool P384r1', 'BrainpoolP384R1', 97),
    '2b240303020801010d': ('Brainpool P512r1', 'BrainpoolP512R1', 129),
}


@dataclass(frozen=True)
class Tlv:
    tag: int
    value: bytes
    encoded: bytes
    start: int


def tlvs(data: bytes) -> list[Tlv]:
    """Bounded CVC framing: one/two-byte tags and minimal definite DER lengths."""
    offset = 0
    result = []
    while offset < len(data):
        start = offset
        tag = data[offset]
        offset += 1
        if tag & 31 == 31:
            if offset == len(data) or not 31 <= data[offset] < 128:
                raise ValueError('Unsupported or nonminimal tag')
            tag = tag << 8 | data[offset]
            offset += 1
        if offset == len(data):
            raise ValueError('Missing length')
        size = data[offset]
        offset += 1
        if size & 128:
            count = size & 127
            if count not in (1, 2) or offset + count > len(data) or data[offset] == 0:
                raise ValueError('Indefinite, oversized or nonminimal length')
            size = int.from_bytes(data[offset:offset + count], 'big')
            offset += count
            if size < 128 or count == 2 and size < 256:
                raise ValueError('Nonminimal length')
        if offset + size > len(data):
            raise ValueError('Truncated value')
        result.append(Tlv(tag, data[offset:offset + size], data[start:offset + size], start))
        offset += size
    return result


@dataclass(frozen=True)
class PublicExample:
    raw: bytes
    body: bytes
    authority: bytes
    holder: bytes
    role: int
    oid: str
    point: bytes
    signature: bytes


def example(raw: bytes) -> PublicExample:
    if not 204 <= len(raw) <= 341:
        raise ValueError('Outside the specified Gen2 certificate range')
    outer = tlvs(raw)
    if len(outer) != 1 or outer[0].tag != 0x7F21:
        raise ValueError('Not exactly one complete certificate container')
    children = tlvs(outer[0].value)
    if [x.tag for x in children] != [0x7F4E, 0x5F37]:
        raise ValueError('Certificate body/signature order differs')
    body = tlvs(children[0].value)
    if [x.tag for x in body] != [0x5F29, 0x42, 0x5F4C, 0x7F49, 0x5F20, 0x5F25, 0x5F24]:
        raise ValueError('Missing, duplicate, reordered or extra body field')
    if [len(body[i].value) for i in (0, 1, 2, 4, 5, 6)] != [1, 8, 7, 8, 4, 4]:
        raise ValueError('Fixed field length differs')
    if body[0].value != b'\x00' or body[2].value[:6] != b'\xFFSMRDT':
        raise ValueError('Not the known Gen2 certificate profile/application')
    key = tlvs(body[3].value)
    if [x.tag for x in key] != [6, 0x86]:
        raise ValueError('Public key field order differs')
    oid = key[0].value.hex()
    if oid not in CURVES or len(key[1].value) != CURVES[oid][2] or key[1].value[0] != 4:
        raise ValueError('Unsupported curve or incorrect uncompressed point framing')
    if len(children[1].value) not in (64, 96, 128, 132):
        raise ValueError('Unexpected plain signature size')
    return PublicExample(raw, children[0].encoded, body[1].value, body[4].value,
        body[2].value[-1], oid, key[1].value, children[1].value)


def signatures(examples: list[PublicExample]) -> dict:
    # Optional local research dependency; no Android/product dependency is added.
    from cryptography.exceptions import InvalidSignature
    from cryptography.hazmat.primitives import hashes
    from cryptography.hazmat.primitives.asymmetric import ec, utils

    def public_key(item):
        return ec.EllipticCurvePublicKey.from_encoded_point(getattr(ec, CURVES[item.oid][1])(), item.point)

    keys = [(item, public_key(item)) for item in examples]
    verified = 0
    for item in examples:
        issuers = {(issuer.oid, issuer.point): key for issuer, key in keys
            if issuer.holder == item.authority and issuer.role in (13, 14)}
        matches = 0
        for key in issuers.values():
            # The issuer's key controls the signature/hash, not the subject's OID.
            component_size = (key.key_size + 7) // 8
            if len(item.signature) != component_size * 2:
                continue
            r = int.from_bytes(item.signature[:component_size], 'big')
            s = int.from_bytes(item.signature[component_size:], 'big')
            digest = hashes.SHA256() if key.key_size <= 256 else hashes.SHA384() if key.key_size <= 384 else hashes.SHA512()
            try:
                key.verify(utils.encode_dss_signature(r, s), item.body, ec.ECDSA(digest))
                matches += 1
            except InvalidSignature:
                pass
        if matches != 1:
            raise ValueError('Public example does not have exactly one distinct matching issuer key')
        verified += 1
    return {'mathematical_example_signatures': verified, 'production_trust_or_current_validity_checked': False}


def framing_rejections(examples: list[PublicExample]) -> int:
    rejected = 0
    for item in examples:
        for raw in (item.raw[:-1], item.raw + b'\x00', b'\x7F\x22' + item.raw[2:]):
            try:
                example(raw)
            except ValueError:
                rejected += 1
            else:
                raise ValueError('A truncated, extended or wrongly tagged example was accepted')
    for raw in (b'\x7F', b'\x7F\x1E\x00', b'\x42\x80', b'\x42\x83\x01\x00\x00',
                b'\x42\x81\x08' + bytes(8), b'\x42\x82\x00\x80' + bytes(128), b'\x42\x08' + bytes(7)):
        try:
            tlvs(raw)
        except ValueError:
            rejected += 1
        else:
            raise ValueError('Malformed tag, length or value was accepted')
    return rejected


def changed_expiry_is_rejected(examples: list[PublicExample]) -> bool:
    raw = examples[0].raw
    outer = tlvs(raw)[0]
    body = tlvs(outer.value)[0]
    expiry = tlvs(body.value)[-1]
    offset = (len(outer.encoded) - len(outer.value) + body.start + len(body.encoded) - len(body.value)
        + expiry.start + len(expiry.encoded) - len(expiry.value))
    changed = bytearray(raw)
    changed[offset] ^= 1
    tampered = example(bytes(changed))  # Still structurally complete; signature must fail.
    try:
        signatures(examples + [tampered])
    except ValueError as error:
        if 'matching issuer key' not in str(error):
            raise
        return True
    raise ValueError('Changing an expiry byte did not invalidate the example signature')


def main():
    args = argparse.ArgumentParser(description=__doc__)
    args.add_argument('archive', type=Path)
    args.add_argument('--verify-signatures', action='store_true')
    options = args.parse_args()
    data = options.archive.read_bytes()
    digest = hashlib.sha256(data).hexdigest()
    if digest != ARCHIVE_SHA256:
        raise ValueError('The input is not the audited official JRC v1.4 archive')
    with zipfile.ZipFile(options.archive) as archive:
        examples = [example(archive.read(info)) for info in archive.infolist() if info.filename.endswith('.cert')]
    if len(examples) != 115:
        raise ValueError('Unexpected public example count')
    result = {'archive_sha256': digest, 'structurally_complete_public_examples': len(examples),
        'malformed_framing_examples_rejected': framing_rejections(examples),
        'length_distribution': dict(sorted(Counter(len(x.raw) for x in examples).items())),
        'curve_distribution': dict(sorted(Counter(CURVES[x.oid][0] for x in examples).items())),
        'mixed_subject_and_issuer_sizes': sorted({(len(x.raw), CURVES[x.oid][0], len(x.point), len(x.signature))
            for x in examples if len(x.signature) != ((len(x.point) - 1) // 2) * 2})}
    if options.verify_signatures:
        result.update(signatures(examples))
        result['structurally_complete_changed_expiry_rejects_signature'] = changed_expiry_is_rejected(examples)
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
