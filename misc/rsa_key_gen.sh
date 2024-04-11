# first, generate private key
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
# then, generate public key
openssl rsa -pubout -in private_key.pem -out public_key.pem
