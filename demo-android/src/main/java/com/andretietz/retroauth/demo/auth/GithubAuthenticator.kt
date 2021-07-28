package com.andretietz.retroauth.demo.auth

import android.accounts.Account
import android.app.Application
import android.content.Context
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.AndroidCredentials
import com.andretietz.retroauth.Authenticator
import com.andretietz.retroauth.CredentialType
import com.andretietz.retroauth.demo.R
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as [Authenticator].
 *
 * If the credential for some reason is invalid, the returning 401 will cause the deletion of the credential and a retry of the
 * call, in which it will get refreshed
 */
class GithubAuthenticator(application: Application) : Authenticator<String, Account, AndroidCredentials>() {

  private val credentialStorage by lazy { AndroidAccountManagerCredentialStorage(application) }
  private val ownerStorage by lazy { AndroidAccountManagerOwnerStorage(application) }

  companion object {
    const val CLIENT_ID = "bb86ddeb2dd22163192f"
    const val CLIENT_SECRET = "0b2a017a3e481c1cb69739ff5a6c4de37009ed7a"
    const val CLIENT_CALLBACK = "https://localhost:8000/accounts/github/login/callback/"
    private const val KEY_TOKEN_VALIDITY = "credential_validity"

    @JvmStatic
    fun createTokenType(context: Context) = CredentialType(
      context.getString(R.string.authentication_TOKEN)
    )
  }

  private val credentialType = createTokenType(application)

  private val ownerType: String = application.getString(R.string.authentication_ACCOUNT)

  override fun getCredentialType(annotationCredentialType: Int): CredentialType = credentialType

  override fun getOwnerType(annotationOwnerType: Int): String = ownerType

  override fun authenticateRequest(request: Request, credential: AndroidCredentials): Request {
    return request.newBuilder()
      .header("Authorization", "Bearer ${credential.token}")
      .build()
  }

  override fun refreshCredentials(
    owner: Account,
    credentialType: CredentialType,
    credential: AndroidCredentials
  ): AndroidCredentials? {
    ownerStorage.createOwner(owner.type, credentialType).get()
    return credentialStorage.getCredentials(owner, credentialType).get()
  }
}
