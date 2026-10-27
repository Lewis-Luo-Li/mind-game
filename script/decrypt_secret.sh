#!/bin/bash

# 1. Verify the passphrase is set
if [ -z "$GPG_PASSPHRASE" ]; then
  echo "Error: GPG_PASSPHRASE environment variable is not set."
  exit 1
fi

# 2. Verify the encrypted file exists
if [ ! -f "secrets/secret.properties.gpg" ]; then
  echo "Error: secrets/secret.properties.gpg not found. Did you run the encryption script first?"
  exit 1
fi

# 3. Securely pipe the passphrase to decrypt the file to the project root
echo "$GPG_PASSPHRASE" | gpg --batch --yes --passphrase-fd 0 --output secret.properties --decrypt secrets/secret.properties.gpg

echo "Success: secrets/secrets.properties.gpg has been decrypted to secret.properties"