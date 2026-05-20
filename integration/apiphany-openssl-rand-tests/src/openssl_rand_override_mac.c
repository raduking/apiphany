/*

CRITICAL WARNING:

This code completely breaks cryptographic security by replacing secure random generation with predictable patterns. Only use this for:

- Debugging SSL/TLS handshakes
- Reproducible testing scenarios
- Educational purposes

For safety this uses a local non-hardened OpenSSL build to avoid any risk of accidentally affecting system-wide OpenSSL behavior. Do NOT use this on production systems or with any sensitive data.

This code overrides the OpenSSL:
	RAND_bytes
	RAND_bytes_ex
	RAND_priv_bytes
functions to provide predictable output for testing purposes.

To compile the dylib on macOS, use the following command (adjusting paths as needed):

gcc -dynamiclib -o openssl_rand_override_mac.dylib openssl_rand_override_mac.c \
  -I$HOME/local/openssl-nonhardened/include \
  -L$HOME/local/openssl-nonhardened/lib \
  -lcrypto \
  -Wl,-undefined,dynamic_lookup

To run the OpenSSL client with the overridden functions, use the following command:

env DYLD_INSERT_LIBRARIES=./openssl_rand_override_mac.dylib \
  DYLD_FORCE_FLAT_NAMESPACE=1 \
  ~/local/openssl-nonhardened/bin/openssl s_client -connect localhost:12345 -tls1_2 -servername localhost -msg -debug

*/

#include <stdio.h>
#include <string.h>
#include <openssl/rand.h>

#define SUCCESS 1
#define FAILURE 0
#define MAX_RANDOM_BYTES 1024

__attribute__((constructor)) void on_load()
{
	fprintf(stderr, "[rk-override] RAND override dylib loaded\n");
}

// Fill the buffer with deterministic bytes (0, 1, 2, ..., num-1)
static int fill_deterministic_bytes(const char *function_name, unsigned char *buf, int num)
{
	if (num < 0 || num > MAX_RANDOM_BYTES)
	{
		fprintf(stderr, "[rk-override] %s: suspicious num=%d, num must be in [0..%d] range, aborting\n", function_name, num, MAX_RANDOM_BYTES);
		return FAILURE;
	}
	for (int i = 0; i < num; ++i)
	{
		buf[i] = (unsigned char)i;
	}
	return SUCCESS;
}

// Replacement for RAND_bytes
int rk_rand_bytes(unsigned char *buf, int num)
{
	fprintf(stderr, "[rk-override] RAND_bytes called for %d bytes\n", num);
	return fill_deterministic_bytes("RAND_bytes", buf, num);
}

// Replacement for RAND_bytes_ex (OpenSSL 3.x)
int rk_rand_bytes_ex(OSSL_LIB_CTX *ctx, unsigned char *buf, int num, unsigned int strength)
{
	fprintf(stderr, "[rk-override] RAND_bytes_ex called for %d bytes, strength=%d\n", num, strength);
	if (num >= 4)
	{
		fprintf(stderr, "[rk-override] Before fill - buf[0..3]: %02x %02x %02x %02x\n",
				buf[0], buf[1], buf[2], buf[3]);
	}
	int result = fill_deterministic_bytes("RAND_bytes_ex", buf, num);
	if (num >= 4)
	{
		fprintf(stderr, "[rk-override] After fill - buf[0..3]: %02x %02x %02x %02x\n",
				buf[0], buf[1], buf[2], buf[3]);
	}
	return result;
}

// Replacement for RAND_priv_bytes
int rk_rand_priv_bytes(unsigned char *buf, int num)
{
	fprintf(stderr, "[rk-override] RAND_priv_bytes called for %d bytes\n", num);
	return fill_deterministic_bytes("RAND_priv_bytes", buf, num);
}

// Interpose struct
__attribute__((used)) static struct
{
	const void *replacement;
	const void *original;
} interposers[] __attribute__((section("__DATA,__interpose"))) = {
	{(const void *)rk_rand_bytes, (const void *)RAND_bytes},
	{(const void *)rk_rand_bytes_ex, (const void *)RAND_bytes_ex},
	{(const void *)rk_rand_priv_bytes, (const void *)RAND_priv_bytes}};
