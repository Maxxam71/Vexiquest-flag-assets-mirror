# Vexiquest Wave 4 Recovery Mirror

Public recovery mirror for **promoted** Vexiquest flag-asset releases.

Primary repository: `Maxxam71/Vexiquest-flag-assets-w4`  
Mirror repository: `Maxxam71/Vexiquest-flag-assets-mirror`

## Rules

- Production releases must contain byte-identical copies of promoted primary assets.
- Identity is proven with full SHA-256 values.
- Tags and asset names are never reused after promotion or deletion.
- Takedown and denylist decisions apply to both primary and mirror.
- No private key, token, confidential evidence, personal data or pre-T2 thumbnail belongs here.
- Large binaries belong in GitHub Releases, never in Git history.
- No repository-wide license is asserted over mirrored flag assets; rights and credits remain item-specific.

## Current gate

`STORAGE_VALID` is pending until the disposable primary/mirror test completes and both temporary releases are removed.

The manual workflow `.github/workflows/storage-smoke-mirror.yml` copies the public disposable primary asset, verifies its SHA-256, verifies the mirrored download and removes the mirrored release and tag.
