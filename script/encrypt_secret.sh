#!/bin/bash

# 1. Verify the passphrase is set to prevent empty passwords
if [ -z "$GPG_PASSPHRASE" ]; then
  echo "Error: GPG_PASSPHRASE environment variable is not set."
  exit 1
fi

# 2. Ensure the output directory exists
mkdir -p secrets

# 3. Securely pipe the passphrase into GPG
echo "$GPG_PASSPHRASE" | gpg --batch --yes --passphrase-fd 0 --output secrets/secret.properties.gpg --symmetric secret.properties

echo "Success: secret.properties has been encrypted to secrets/secrets.properties.gpg"