#!/usr/bin/env bash
# Encrypt/decrypt secret.properties (AdMob IDs) with GPG symmetric encryption.
#
# Usage:
#   scripts/secret-properties.sh encrypt   # secret.properties -> secret.properties.gpg (commit this)
#   scripts/secret-properties.sh decrypt   # secret.properties.gpg -> secret.properties (gitignored)
#
# The passphrase comes from the GPG_PASSPHRASE environment variable.
# Unset: gpg prompts interactively (local use — never stored on disk).
# GitHub Actions sets it from the GPG_PASSPHRASE repo secret.
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
PLAIN="secret.properties"
ENC="secret.properties.gpg"

case "${1:-}" in
    encrypt)
        [[ -f "$PLAIN" ]] || { echo "error: $PLAIN not found" >&2; exit 1; }
        if [[ -n "${GPG_PASSPHRASE:-}" ]]; then
            gpg --batch --yes --symmetric --cipher-algo AES256 \
                --passphrase "$GPG_PASSPHRASE" --output "$ENC" "$PLAIN"
        else
            gpg --symmetric --cipher-algo AES256 --output "$ENC" "$PLAIN"
        fi
        echo "wrote $ENC"
        ;;
    decrypt)
        [[ -f "$ENC" ]] || { echo "error: $ENC not found" >&2; exit 1; }
        if [[ -n "${GPG_PASSPHRASE:-}" ]]; then
            gpg --batch --yes --decrypt \
                --passphrase "$GPG_PASSPHRASE" --output "$PLAIN" "$ENC"
        else
            gpg --decrypt --output "$PLAIN" "$ENC"
        fi
        echo "wrote $PLAIN"
        ;;
    *)
        echo "usage: $0 encrypt|decrypt" >&2
        exit 2
        ;;
esac
