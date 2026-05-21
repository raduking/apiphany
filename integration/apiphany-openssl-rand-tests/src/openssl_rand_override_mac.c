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
	if (num == 0)
	{
		fprintf(stderr, "[rk-override] %s: num=0, nothing to fill\n", function_name);
		return SUCCESS;
	}
	fprintf(stderr, "[rk-override] Before fill - buf[0..%d]: ", num - 1);
	for (int i = 0; i < num; ++i)
	{
		fprintf(stderr, "%02x ", buf[i]);
	}
	fprintf(stderr, "\n");
	fprintf(stderr, "[rk-override] After fill - buf[0..%d]: ", num - 1);
	for (int i = 0; i < num; ++i)
	{
		buf[i] = (unsigned char)i;
		fprintf(stderr, "%02x ", buf[i]);
	}
	fprintf(stderr, "\n");
	return SUCCESS;
}

// Replacement for RAND_bytes
int rk_rand_bytes(unsigned char *buf, int num)
{
	fprintf(stderr, "[rk-override] RAND_bytes called for %d bytes\n", num);
	return fill_deterministic_bytes("RAND_bytes", buf, num);
}

// Replacement for RAND_bytes_ex (OpenSSL 3.x)
int rk_rand_bytes_ex(OSSL_LIB_CTX *ctx, unsigned char *buf, size_t num, unsigned int strength)
{
	fprintf(stderr, "[rk-override] RAND_bytes_ex called for %zu bytes, strength=%u\n", num, strength);
	if (num > MAX_RANDOM_BYTES)
	{
		fprintf(stderr, "[rk-override] RAND_bytes_ex: num=%zu exceeds MAX_RANDOM_BYTES=%d, aborting\n", num, MAX_RANDOM_BYTES);
		return FAILURE;
	}
	if (num > INT_MAX)
	{
		fprintf(stderr, "[rk-override] RAND_bytes_ex: num=%zu exceeds INT_MAX, would overflow, aborting\n", num);
		return FAILURE;
	}
	return fill_deterministic_bytes("RAND_bytes_ex", buf, (int)num);
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
	{rk_rand_bytes, RAND_bytes},
	{rk_rand_bytes_ex, RAND_bytes_ex},
	{rk_rand_priv_bytes, RAND_priv_bytes}};
