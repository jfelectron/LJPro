package org.scribe.oauth;

import java.util.HashMap;

import org.scribe.builder.api.*;
import org.scribe.model.*;

/**
 * OAuth 1.0a implementation of {@link OAuthService}
 * 
 * @author Pablo Fernandez
 */
public class OAuth10aServiceImpl implements OAuthService
{
  private static final String NO_SCOPE = null;
  private static final String VERSION = "1.0";

  private OAuthConfig config;
  private DefaultApi10a api;
  private String scope;

  /**
   * Default constructor
   * 
   * @param api OAuth1.0a api information
   * @param config OAuth 1.0a configuration param object
   */
  public OAuth10aServiceImpl(DefaultApi10a api, OAuthConfig config)
  {
    this.api = api;
    this.config = config;
    this.scope = NO_SCOPE;
  }

  /**
   * {@inheritDoc}
   */
  public Token getRequestToken()
  {
    OAuthRequest request = new OAuthRequest(api.getRequestTokenVerb(), api.getRequestTokenEndpoint());
    addOAuthParams(request, OAuthConstants.EMPTY_TOKEN);
    //addOAuthHeader(request);
    Response response = request.send();
    return api.getRequestTokenExtractor().extract(response.getBody());
  }

  public void addOAuthParams(OAuthRequest request, Token token)
  {
    request.addBodyParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
    request.addBodyParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
    request.addBodyParameter(OAuthConstants.CONSUMER_KEY, config.getApiKey());
    request.addBodyParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
    request.addBodyParameter(OAuthConstants.VERSION, getVersion());
    //request.addOAuthParameter(OAuthConstants.CALLBACK, config.getCallback());
    if(scope != NO_SCOPE) request.addBodyParameter(OAuthConstants.SCOPE, scope);
    request.addBodyParameter(OAuthConstants.SIGNATURE, getSignature(request, token));
  }

  /**
   * {@inheritDoc}
   */
  public HashMap<String,Object> getAccessToken(Token requestToken, Verifier verifier)
  {
    OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
    request.addBodyParameter(OAuthConstants.TOKEN, requestToken.getToken());
    //request.addOAuthParameter(OAuthConstants.VERIFIER, verifier.getValue());
    addOAuthParams(request, requestToken);
    //addOAuthHeader(request);
    Response response = request.send();
    HashMap<String,Object> respParams=new HashMap<String,Object>();
    respParams.put("response", response.getBody());
    respParams.put("token",api.getAccessTokenExtractor().extract(response.getBody()));
    return respParams;
  }

  /**
   * {@inheritDoc}
   */
  public void signRequest(Token token, OAuthRequest request)
  {
    request.addBodyParameter(OAuthConstants.TOKEN, token.getToken());
    addOAuthParams(request, token);
    //addOAuthHeader(request);
  }

  /**
   * {@inheritDoc}
   */
  public String getVersion()
  {
    return VERSION;
  }

  /**
   * {@inheritDoc}
   */
  public void addScope(String scope)
  {
    this.scope = scope;
  }

  /**
   * {@inheritDoc}
   */
  public String getAuthorizationUrl(Token requestToken)
  {
    return api.getAuthorizationUrl(requestToken);
  }
  
  private String getSignature(OAuthRequest request, Token token)
  {
    String baseString = api.getBaseStringExtractor().extract(request);
    return api.getSignatureService().getSignature(baseString, config.getApiSecret(), token.getSecret());
  }

  private void addOAuthHeader(OAuthRequest request)
  {
    String oauthHeader = api.getHeaderExtractor().extract(request);
    request.addHeader(OAuthConstants.HEADER, oauthHeader);
  }
}
