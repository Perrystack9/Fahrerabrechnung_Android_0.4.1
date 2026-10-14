# Independent public/synthetic mathematical vectors

`vectors.zip`: SHA256 `267440c8fdeb3240e155c55f1ac7a6bc3580152cac51612d009b31dc76817dfc`.
74 binary files plus `manifest.json` with per-file hashes. Python cryptography50.0.2 / OpenSSL4.0.3 created and verified these independently of Bouncy Castle. Private keys existed only in RAM and are not stored here. Public JRC artifacts and synthetic certificates are documented in `docs/CARD_CORE_SIGNATURE_MATH_EVIDENCE.md`.

RSA covers synthetic root/CA/card, both complete EF files, strict-frame negatives and a public MSCA known answer. EC covers all six allowed curves, both full EF files, ordinary and double-hashed signatures, deliberately unauthenticated/expired C101 and one valid plain R beginning0x30. Signatures use full fixed unsigned components, not DER. The generator is `scripts/generate-core-math-vectors.py`; randomness gives new answers, not byte-identical regeneration.

JUnit checks the exact archive and every entry digest. Android instrumentation loads the same archive from test assets; these assets do not enter the product APK.
