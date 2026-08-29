#!/usr/bin/env bash
# Run once from the repo root before your first `docker build`, unless
# gym-crm/src/main/resources/keystore/dev-keystore.p12 already exists
# (e.g. it's already used by the Cucumber test suite).
#
# gym-crm signs JWTs with this keystore's private key (see JwtConfig);
# gym-crm-workload verifies them with the matching public key.
set -euo pipefail

KEYSTORE_DIR="src/main/resources/keystore"
KEYS_DIR="gym-crm-workload/src/main/resources/keys"
ALIAS="gym-crm-auth"
PASSWORD="dev-keystore-pass"

mkdir -p "$KEYSTORE_DIR" "$KEYS_DIR"

if [ -f "$KEYSTORE_DIR/dev-keystore.p12" ]; then
  echo "Keystore already present at $KEYSTORE_DIR/dev-keystore.p12 - skipping generation."
else
  keytool -genkeypair \
    -alias "$ALIAS" \
    -keyalg RSA -keysize 2048 \
    -storetype PKCS12 \
    -keystore "$KEYSTORE_DIR/dev-keystore.p12" \
    -storepass "$PASSWORD" -keypass "$PASSWORD" \
    -validity 3650 \
    -dname "CN=gym-crm, OU=dev, O=provokedynamic, L=Kutaisi, ST=Imereti, C=GE"
  echo "Created $KEYSTORE_DIR/dev-keystore.p12"
fi

keytool -exportcert \
  -alias "$ALIAS" \
  -keystore "$KEYSTORE_DIR/dev-keystore.p12" \
  -storepass "$PASSWORD" \
  -rfc | openssl x509 -pubkey -noout > "$KEYS_DIR/gym-crm-public.pem"

echo "Exported public key to $KEYS_DIR/gym-crm-public.pem"
