package com.andretietz.retroauth

import com.andretietz.retroauth.testimpl.TestTokenStorage
import com.andretietz.retroauth.testimpl.TestTokenTypeFactory
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class CredentialInterceptorTest {


    companion object {
        val AUTHENTICATION_HEADER_KEY = "auth"
    }



    @Test
    fun intercept() {
        val request1 = Request.Builder().url("http://www.google.de/test1").build()
        val request2 = Request.Builder().url("http://www.google.de/test2").build()

        // only request2 is authenticated
        val authHandler = getAuthenticationHandler(request2)
        val interceptor = CredentialInterceptor(authHandler)

        val interceptorChain = TestInterceptorChain()

        // testing unauthenticated request
        interceptorChain.setupRequest(request1)
        var response = interceptor.intercept(interceptorChain)
        // should not contain any headers
        Assert.assertEquals(0, response!!.headers().size().toLong())

        // testing authenticated request
        interceptorChain.setupRequest(request2)
        response = interceptor.intercept(interceptorChain)
        // should contain the token in the header
        Assert.assertTrue(TestTokenStorage.TEST_TOKEN == response!!.request().header(AUTHENTICATION_HEADER_KEY))

    }

    private fun getAuthenticationHandler(request: Request): AuthenticationHandler<String, String, String> {
        val methodCache = MethodCache.DefaultMethodCache<String>()
        methodCache.register(Utils.createUniqueIdentifier(request), "token-type")

        return AuthenticationHandler(
                methodCache,
                object : OwnerManager<String, String> {
                    override fun getOwner(type: String): String = "owner"
                },
                TestTokenStorage(),
                object : TokenProvider<String> {
                    override fun authenticateRequest(request: Request, token: String): Request {
                        return request.newBuilder().addHeader(AUTHENTICATION_HEADER_KEY, token).build()
                    }
                },
                TestTokenTypeFactory()
        )
    }

    internal class TestInterceptorChain : Interceptor.Chain {
        private var request: Request? = null

        fun setupRequest(request: Request) {
            this.request = request
        }

        override fun request(): Request? {
            return request
        }

        @Throws(IOException::class)
        override fun proceed(request: Request): Response {
            return Response.Builder()
                    .request(request)
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .message("required message")
                    .build()
        }

        override fun connection(): Connection? {
            return null
        }
    }
}
