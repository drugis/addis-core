NAME=addis-daan
PASS=develop

# import existing certificate & private key to PKCS12 store
openssl pkcs12 -export -inkey $NAME-key.pem -in $NAME-crt.pem -certfile ca-crt.pem -out $NAME.p12 -passout pass:$PASS
# generate JKS for said pair
keytool -importkeystore -srckeystore $NAME.p12 -keystore $NAME.jks -alias 1 -srcstoretype PKCS12 -deststorepass $PASS -srcstorepass $PASS
# add the signing CA's certificate to the JKS
keytool -import -keystore $NAME.jks -file ca-crt.pem -alias drugis-ca -deststorepass $PASS

# add CA to trust store
keytool -importcert -file ca-crt.pem -alias drugisCA -keystore drugis-ca.jks
