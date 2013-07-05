package play.api.libs.oauth


import _root_.oauth.signpost.exception.OAuthException
import _root_.oauth.signpost.basic.DefaultOAuthConsumer
import _root_.oauth.signpost.commonshttp.CommonsHttpOAuthProvider
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpHost
import org.apache.http.conn.params.ConnRoutePNames

case class OAuthSupportProxy (info: ServiceInfo,  use10a: Boolean = true , proxy:Option[(String,Int)]=None  ){

  private val provider = {
    proxy match{
      case None => {
	    val p = new CommonsHttpOAuthProvider(info.requestTokenURL, info.accessTokenURL, info.authorizationURL)
	    p.setOAuth10a(use10a)
	    p
      }
      case Some( (host,port)) =>{
	    val p = new CommonsHttpOAuthProvider(info.requestTokenURL, info.accessTokenURL, info.authorizationURL)
	    p.setOAuth10a(use10a)
	    val httpclient: DefaultHttpClient = new DefaultHttpClient()
	    val proxy = new HttpHost(host, port);
        httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	    p.setHttpClient( httpclient )
	    p
      }
    }
  }

  /**
   * Request the request token and secret.
   *
   * @param callbackURL the URL where the provider should redirect to (usually a URL on the current app)
   * @return A Right(RequestToken) in case of success, Left(OAuthException) otherwise
   */
  def retrieveRequestToken(callbackURL: String): Either[OAuthException, RequestToken] = {
    val consumer = new DefaultOAuthConsumer(info.key.key, info.key.secret)
    try {
      provider.retrieveRequestToken(consumer, callbackURL)
      Right(RequestToken(consumer.getToken(), consumer.getTokenSecret()))
    } catch {
      case e: OAuthException => Left(e)
    }
  }

  /**
   * Exchange a request token for an access token.
   *
   * @param the token/secret pair obtained from a previous call
   * @param verifier a string you got through your user, with redirection
   * @return A Right(RequestToken) in case of success, Left(OAuthException) otherwise
   */
  def retrieveAccessToken(token: RequestToken, verifier: String): Either[OAuthException, RequestToken] = {
    val consumer = new DefaultOAuthConsumer(info.key.key, info.key.secret)
    consumer.setTokenWithSecret(token.token, token.secret)
    try {
      provider.retrieveAccessToken(consumer, verifier)
      Right(RequestToken(consumer.getToken(), consumer.getTokenSecret()))
    } catch {
      case e: OAuthException => Left(e)
    }
  }

  /**
   * The URL where the user needs to be redirected to grant authorization to your application.
   *
   * @param token request token
   */
  def redirectUrl(token: String): String = {
    _root_.oauth.signpost.OAuth.addQueryParameters(
      provider.getAuthorizationWebsiteUrl(),
      _root_.oauth.signpost.OAuth.OAUTH_TOKEN,
      token
    );
  }

}