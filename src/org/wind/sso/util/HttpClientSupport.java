package org.wind.sso.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *@描述：HtppClient的2次封装工具（包含：get和post方法）
 *@版权：鼎好国际——胡璐璐
 *@时间：2014年4月25日 15:47:47 
 */
@SuppressWarnings("unchecked")
public class HttpClientSupport {
	
	private final static Logger logger = Logger.getLogger(HttpClientSupport.class);
	private HttpClient hc;		//当前HttpClient
	private HttpClientContext httpContext;	//当前HttpClient的上下文
	public int gzipLength = 1;		//GZIP每循环一次取多长的数据
	private static HttpClientSupport httpClientSupport;
	
	protected static final int OK = 200;	
	protected static final int NOT_MODIFIED= 304;		//目标数据不是最新的（未修改）
	protected static final int BAD_REQUEST= 400;		//无效请求（有可能是目标服务器主动报的错）
	protected static final int NOT_AUTHORIZED = 401;		//未授权:身份验证凭证缺失或不正确的
	protected static final int FORBIDDEN=403;	//被目标服务器封禁
	protected static final int NOT_FOUND=404;
	protected static final int NOT_ACCEPTABLE=406;	
	protected static final int INTERNAL_SERVER_ERROR =500;		//目标服务器内部出错
	protected static final int BAD_GATEWAY=502;		//目标服务器升级，或 网关有错
	protected static final int SERVICE_UNAVAILABLE=503;	//目标服务不可用
 
    /*******【自4.5版本】——2016年1月22日 14:12:14*******/
    private static PoolingHttpClientConnectionManager pcm;			//连接池配置
    private static RequestConfig config;		//请求配置
    private static SSLConnectionSocketFactory sslContent;		//SSL协议配置
    
    /**最大连接数**/  
    public static Integer MAX_TOTAL_CONNECTIONS;  
    /**每个目标IP最大连接数**/  
    public static Integer MAX_ROUTE_CONNECTIONS;  
    /**连接超时时间**/  
    public static Integer CONNECT_TIMEOUT;  
    /**读取超时时间（数据传输超时时间）**/  
    public static Integer READ_TIMEOUT; 
    
    private HttpClientSupport() {
    	MAX_TOTAL_CONNECTIONS = SysConstant.conPoolSize;
    	CONNECT_TIMEOUT = SysConstant.conTimeout;
    	READ_TIMEOUT = SysConstant.readTimeout;
    	MAX_ROUTE_CONNECTIONS = SysConstant.routeConSize;
        
        /************4.5版本**********/
    	//请求配置
    	Builder requestConfig_builder=RequestConfig.custom();
    	requestConfig_builder.setCookieSpec(CookieSpecs.STANDARD_STRICT);		//Cookie政策——严格标准
        requestConfig_builder.setExpectContinueEnabled(true);		//是否可以继续使用
        requestConfig_builder.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,AuthSchemes.DIGEST));	//设置目标首选的身份验证方案
        requestConfig_builder.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));	//设置代理首选的身份验证方案
        requestConfig_builder.setConnectTimeout(CONNECT_TIMEOUT);		//连接超时设置
        requestConfig_builder.setSocketTimeout(READ_TIMEOUT);	//读取超时
        config=requestConfig_builder.build();
        
        //协议配置
        sslContent=getCancelSSL();		//SSL配置对象
        RegistryBuilder<ConnectionSocketFactory> registry_build=RegistryBuilder.create();
        registry_build.register("http", PlainConnectionSocketFactory.INSTANCE);
        registry_build.register("https", sslContent);
        Registry<ConnectionSocketFactory> reg_socketFactory=registry_build.build();
        
        //连接池配置
        pcm=new PoolingHttpClientConnectionManager(reg_socketFactory);
        pcm.setMaxTotal(MAX_TOTAL_CONNECTIONS); 
        pcm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);  	//每个Client最大可以有多少个不同域的请求
    }
    
    //获取 : 取消（忽略）SSL的验证的SSL配置工程对象
    private SSLConnectionSocketFactory getCancelSSL(){
    	SSLConnectionSocketFactory t_sslContent = null;
    	try{
    		SSLContext ctx = SSLContext.getInstance("TLS");
    		X509TrustManager tm = new X509TrustManager() {
    			public X509Certificate[] getAcceptedIssuers() {return null;}
    			public void checkServerTrusted(X509Certificate[] chain,String authType) throws CertificateException {}
    			public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException {}
    		};
    		ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
    		//所有主机验证
//    		t_sslContent=new SSLConnectionSocketFactory(ctx,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    		//等待实例主机验证
    		t_sslContent=new SSLConnectionSocketFactory(ctx,NoopHostnameVerifier.INSTANCE);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return t_sslContent;
    }
    
    /*获取：HttpClientSupport实例*/
    public synchronized static HttpClientSupport getInstance() {
    	if(httpClientSupport==null){
    		httpClientSupport = new HttpClientSupport();
    	}
    	return httpClientSupport;
    }
    /*实例化：带httpClient*/
    public HttpClientSupport(HttpClient client){
    	this.httpContext=HttpClientContext.create();
    	this.hc=client;
    }
    //获取HttpClientBuilder（4.3以上）
    private HttpClientBuilder getHttpClientBuilder(){
    	HttpClientBuilder httpClientBuilder=HttpClients.custom();
    	httpClientBuilder.setConnectionManager(pcm);
    	httpClientBuilder.setDefaultRequestConfig(config);
    	return httpClientBuilder;
    }
    /*创建HttpClientSupport*/
    public HttpClientSupport generatorHttpClient(){
    	return new HttpClientSupport(this.getHttpClientBuilder().build());
    }
    //获取HttpClient
    public HttpClient getHttpClient(){
    	return this.hc;
    }
    //获取HttpContext
    public HttpClientContext getHttpClientContext(){
    	return this.httpContext;
    }
    //获取CookieStore
    public CookieStore getCookieStore(){
    	return this.httpContext.getCookieStore();
    }
    //设置CookieStore
    public void setCookieStore(CookieStore cookieStore){
    	this.httpContext.setCookieStore(cookieStore);
    }
    /**
     * post提交（-6：字符集默认“UTF-8”，json数据）
     * @param url :提交的URL
     * @param json : post提交的json参数值
     * @param headMap : 头部参数（Map式）
     * @param charSet : 字符集
     * @throws Exception 
     * */
    public Response post_json(String url,String json,Map<String,String> headMap,String charSet) throws Exception {
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	charSet=charSet!=null && charSet.trim().length()>0?charSet.trim():"UTF-8";
    	StringEntity entity = new StringEntity(json,charSet);//解决中文乱码问题    
		entity.setContentEncoding(charSet);    
		entity.setContentType("application/json");
		Iterator<String> key= headMap.keySet().iterator();											//Key【头部名称】
    	int i=0;
    	while(key.hasNext()){
    		String t_key=key.next();
    		headArr[i]=new BasicHeader(t_key,(String) headMap.get(t_key));
    		i++;
    	}
		return post(hc,url,charSet,entity,headArr);
    	
    }
    /**
     * post提交（-5：字符集默认"UTF-8"）
     * @param url：提交的URL
     * @param paramMap : post提交的参数（Map式）
     * @param headMap : 头部参数（Map式）
     * @param isUpload : 是否提交上传文件表单（如：multipart/form-data类型的）
     * @throws Exception 
     * */
    public Response post(String url,Map<String,String> paramMap,Map<String,String> headMap,boolean isUpload,String... charSet) throws Exception {
    	String charSet_return = null;
		if(charSet!=null && charSet.length>1 && charSet[1]!=null){
			charSet_return=charSet[1];
		}else{
			charSet_return="UTF-8";
		}
		//是否文件上传
    	if(isUpload){
    		try {
    			headMap=headMap!=null?headMap:new HashMap<String,String>();
    	    	Header headArr[] = new Header[headMap.size()];
	    		MultipartEntityBuilder builder=MultipartEntityBuilder.create();
	    		builder.setCharset(Charset.forName(charSet_return));//设置请求的编码格式
	    		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    		ContentType contentType= ContentType.create("text/plain", charSet_return);
    			for(Entry<String,String> entry:paramMap.entrySet()){
    				String key=entry.getKey();
    				String value=entry.getValue();
    				File file=new File(value);
    				//该参数为文件路径
    				if(file.isFile()){
    					builder.addPart(key, new FileBody(file));
    				}else{
    					builder.addPart(key, new StringBody(value, contentType));
    				}
    			}
    			HttpEntity reqEntity= builder.build();
    			int i=0;
    			for(Entry<String,String> entry:headMap.entrySet()){
    	    		String key=entry.getKey();
    	    		String value=entry.getValue();
    	    		headArr[i]=new BasicHeader(key,value);
    	    		i++;
    	    	}
    	    	return post(hc,url,charSet_return,reqEntity,headArr);
    		} catch (Exception e) {
    			throw e;
			}
    	}else{
    		return post(url, paramMap, headMap,charSet_return);
    	}
    }
    /**
     * post提交（-4：字符集默认"UTF-8"）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @param charset : 字符集，参数、返回的字符，不传则为UTF-8
     * */
    public Response post(HttpClient hc,String url,Map<String,String> paramMap,Map<String,String> headMap,String... charset) throws Exception {
    	UrlEncodedFormEntity reqEntity = null;
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>();
    	String charset_t="UTF-8";
    	if(charset!=null && charset.length>0 && charset[0]!=null){
    		charset_t=charset[0];
    	}
    	try {
			//如果没有参数
			if(paramMap!=null){
	    		Iterator<String> key= paramMap.keySet().iterator();									//Key【参数名称】
	    		while(key.hasNext()){
	    			String t_key=key.next();
	    			formParams.add(new BasicNameValuePair(t_key,String.valueOf(paramMap.get(t_key))));
	    		}
    		}
			reqEntity = new UrlEncodedFormEntity(formParams,charset_t);
			Iterator<String> key= headMap.keySet().iterator();											//Key【头部名称】
	    	int i=0;
	    	while(key.hasNext()){
	    		String t_key=key.next();
	    		headArr[i]=new BasicHeader(t_key,(String) headMap.get(t_key));
	    		i++;
	    	}
	    	return post(hc,url,charset_t,reqEntity,headArr);
    	} catch (Exception e) {
    		throw e;
		}
    }
    /**
     * post提交（-3：字符集默认“UTF-8”，XML、JSON等大容量数据）
     * @param url :提交的URL
     * @param xml : post提交的参数
     * @param headMap : 头部参数（Map式）
     * @param charSet : 字符集
     * @param isGZip : 是否对xml压缩GZip
     * @throws Exception 
     * */
    public Response post(String url,String xml,Map<String,String> headMap,String charSet,boolean isGZip) throws Exception {
    	ByteArrayEntity reqEntity;
    	headMap=headMap!=null?headMap:new HashMap<String,String>();
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	ByteArrayInputStream bais=null;
		ByteArrayOutputStream baos=null;
		GZIPOutputStream gos = null;
		try {
			byte ch[]=xml.getBytes(charSet);
			if(isGZip){
				bais = new ByteArrayInputStream(ch);
				baos = new ByteArrayOutputStream();
				gos = new GZIPOutputStream(baos); 
				int count;   
				byte data[] = new byte[1024];
				while ((count = bais.read(data, 0, 1024)) != -1) {   
					gos.write(data, 0, count);   
				}   
				ch=baos.toByteArray();
				gos.finish();   
				gos.flush();
			}
			reqEntity = new ByteArrayEntity(ch);
	    	Iterator<String >key= headMap.keySet().iterator();											//Key【头部名称】
	    	int i=0;
	    	while(key.hasNext()){
	    		String t_key=key.next();
	    		headArr[i]=new BasicHeader(t_key,headMap.get(t_key));
	    		i++;
	    	}
	    	return post(hc,url,"UTF-8",reqEntity,headArr);
		} catch (Exception e) {
			throw e;
		}finally{
			if(bais!=null){try{bais.close();}catch(Exception e1){}}
			if(baos!=null){try{baos.close();}catch(Exception e1){}}
			if(gos!=null){try{gos.close();}catch(Exception e1){}}
		}
    }
    /**
     * post提交（-2：字符集默认“UTF-8”，XML数据）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @throws Exception 
     * */
    public Response post(String url,String xml,Map<String,String> headMap) throws Exception {
    	ByteArrayEntity reqEntity;
    	headMap=headMap!=null?headMap:new HashMap<String, String>();
    	Header headArr[] = new Header[headMap.size()];
		try {
			reqEntity = new ByteArrayEntity(xml.getBytes("UTF-8"));
	    	Iterator<String >key= headMap.keySet().iterator();											//Key【头部名称】
	    	int i=0;
	    	while(key.hasNext()){
	    		String t_key=key.next();
	    		headArr[i]=new BasicHeader(t_key,headMap.get(t_key));
	    		i++;
	    	}
	    	return post(hc,url,"UTF-8",reqEntity,headArr);
		} catch (Exception e) {
			throw e;
		}
    }
    /**
     * post提交（-1：字符集默认"UTF-8"）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @throws Exception 
     * */
    public Response post(String url,Map<String,String> paramMap,Map<String,String> headMap) throws Exception {
    	return post(url,paramMap,headMap,"UTF-8");
    }
    /**
     * post提交（0：可用Map传头部信息）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @param charset：字符编码集（可做参数字符，也可做返回的数据转换的字符编码）
     * @throws Exception 
     * */
    public Response post(String url,Map<String,String> paramMap,Map<String,String> headMap ,String charset) throws Exception {
    	UrlEncodedFormEntity reqEntity = null;
    	headMap=headMap!=null?headMap:new HashMap<String,String>();
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>();
    	try {
			//如果有参数
			if(paramMap!=null){
	    		Iterator<String> key= paramMap.keySet().iterator();									//Key【参数名称】
	    		while(key.hasNext()){
	    			String t_key=key.next();
	    			formParams.add(new BasicNameValuePair(t_key,paramMap.get(t_key)));
	    		}
    		}
			reqEntity = new UrlEncodedFormEntity(formParams,charset);
			Iterator<String> key= headMap.keySet().iterator();											//Key【头部名称】
	    	int i=0;
	    	while(key.hasNext()){
	    		String t_key=key.next();
	    		headArr[i]=new BasicHeader(t_key,headMap.get(t_key));
	    		i++;
	    	}
	    	return post(hc,url,charset,reqEntity,headArr);
    	} catch (Exception e) {
    		throw e;
		}
    }
    /**
     * post提交（A-1：一对多，字符集默认"UTF-8"）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @throws Exception 
     * */
    public Response postList(String url,Map<String,Object> paramMap,Map<String,String> headMap) throws Exception {
    	return postList(url,paramMap,headMap,"UTF-8");
    }
    /**
     * post提交（A0：一对多）
     * @param url：提交的URL
     * @param paramMap：post提交的参数（Map式）
     * @param headMap：头部参数（Map式）
     * @param charset：字符编码集（可做参数字符，也可做返回的数据转换的字符编码）
     * @throws Exception 
     * */
    public Response postList(String url,Map<String,Object> paramMap,Map<String,String> headMap ,String charset) throws Exception {
    	UrlEncodedFormEntity reqEntity = null;
    	headMap=headMap!=null?headMap:new HashMap<String,String>();
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>();
    	try {
			//如果有参数
			if(paramMap!=null){
	    		for(Entry<String,Object> entry:paramMap.entrySet()){
	    			String key=entry.getKey();
	    			Object value=entry.getValue();
	    			if(value instanceof List){
						List<Object> valueList=(List<Object>) value;
	    				for(Object t_value:valueList){
	    					formParams.add(new BasicNameValuePair(key,t_value+""));
	    				}
	    			}else{
	    				formParams.add(new BasicNameValuePair(key,value+""));
	    			}
	    		}
    		}
			reqEntity = new UrlEncodedFormEntity(formParams,charset);
	    	int i=0;
	    	for(Entry<String,String> entry:headMap.entrySet()){
	    		headArr[i]=new BasicHeader(entry.getKey(),entry.getValue());
	    		i++;
	    	}
	    	return post(hc,url,charset,reqEntity,headArr);
    	} catch (Exception e) {
    		throw e;
		}
    }
    /**post提交（1）*/
    public Response post(HttpClient client , String url, String charset,HttpEntity reqEntity) throws HttpClientExecuteException{
    	return post(client ,url,charset,reqEntity,new Header[0]);
    }
    /**post提交（2）*/
    public Response post(HttpClient client,String url,String charset,HttpEntity reqEntity , Header... reqHeaders) throws HttpClientExecuteException{
		Response res = null;
		int responseCode = -1;
		HttpPost post = null;
		HttpResponse httpResponse ;
		String location=null;
		HttpEntity resultEntity=null;		//请求结果实体类
		try {
			post = new HttpPost(url);
			post.setEntity(reqEntity);
			if(reqHeaders.length>0){
				post.setHeaders(reqHeaders);
			}
			httpResponse = client.execute(post,this.httpContext);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			//正常
			if (responseCode == OK) {
				res = dealResponse(httpResponse,charset,responseCode);
			//HttpStatus：状态码类（如：301、302、303、404、500、200等等，这里判断是否为30开头的跳转）
			} else if((responseCode+"").indexOf("30")!=-1){
				Header[] headers = httpResponse.getHeaders("location");
    	    	String newUrl = headers[0].getValue();
    	    	location=newUrl;
    	    	//未带有域时（比如：http），取host拼接起来
    	    	if(!newUrl.startsWith("http://") && !newUrl.startsWith("https://")){
    	    		Header[] postHeaders = post.getHeaders("Host");
    	    		if(postHeaders!=null && postHeaders.length>0){
    	    			newUrl = "http://"+postHeaders[0].getValue()+newUrl;
    	    		}
    	    	}
    	    	HttpGet newGet = new HttpGet(newUrl);
    			for(Header header:reqHeaders){
	    			if(header.getName().equals("User-Agent")){
	    				newGet.setHeader(header);
	    			}
	    			if(header.getName().equals("Referer")){
	    				newGet.setHeader(header);
	    			}
	    		}
    			EntityUtils.consume(httpResponse.getEntity());
    			HttpResponse responseNew = client.execute(newGet,this.httpContext);
    			res = dealResponse(responseNew,charset,responseCode);
			} else {
				resultEntity=httpResponse.getEntity();
				throw new HttpClientExecuteException(getCause(responseCode) + "\n" , responseCode);
			}
		} catch (SocketTimeoutException e){
			responseCode=408;
		} catch(ConnectTimeoutException e){
			responseCode=408;
		} catch(HttpClientExecuteException e){
			throw e;
		} catch (IOException ioe) {
			throw new HttpClientExecuteException(ioe.getMessage(), ioe, responseCode);
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			try {if(resultEntity!=null){EntityUtils.consume(resultEntity);}} catch (IOException e) {}
			if(post!=null){
				post.abort();
			}
		}
		if(res==null){
			res = new Response(responseCode);
		}
		res.setLocation(location);
		return res;
    }

    /**get提交（-3：可用Map传头部信息,编码默认“UTF-8”）*/
    public Response get(String url ,Map<String,String> headMap) throws HttpClientExecuteException, IOException {
    	Header headArr[] = new Header[headMap!=null?headMap.size():0];
    	Iterator<String> key= headMap.keySet().iterator();				//Key【头部名称】
    	int i=0;
    	while(key.hasNext()){
    		String keyStr=key.next();
    		headArr[i]=new BasicHeader(keyStr,headMap.get(keyStr));
    		i++;
    	}
    	return get(hc,url,"UTF-8",headArr);
    }

    /**get提交（-2：其他的参数默认）*/
    public Response get(String url) throws HttpClientExecuteException, IOException {
    	return get(hc,url,"UTF-8",new Header[0]);
    }
    /**get提交（-1：只传charset字符编码）*/
    public Response get(String url,String charset) throws HttpClientExecuteException, IOException {
    	return get(hc,url,charset,new Header[0]);
    }
    /**get提交（0：可用Map传头部信息）*/
    public Response get(String url ,String charset,Map<String,String> headMap) throws HttpClientExecuteException, IOException {
    	Header headArr[] = new Header[headMap.size()];
    	Iterator<String> key= headMap.keySet().iterator();				//Key【头部名称】
    	int i=0;
    	while(key.hasNext()){
    		String keyStr=key.next();
    		headArr[i]=new BasicHeader(keyStr,headMap.get(keyStr));
    		i++;
    	}
    	return get(hc,url,charset,headArr);
    }
    
    /**get提交（1）*/
    public Response get(HttpClient client , String url,String charset) throws HttpClientExecuteException, IOException{
    	return get(client,url,charset,new Header[0]);
    }
    /**get提交（2）*/
    public Response get(HttpClient client,String url ,String charset,Header... reqHeaders) throws HttpClientExecuteException, IOException{
    	Response res = null;
		int responseCode = -1;		//未知错误
		HttpResponse httpResponse = null;
		HttpGet get = null;
		String location=null;
		HttpEntity resultEntity=null;		//请求结果类
		try {
			get = new HttpGet(url);
			if(reqHeaders!=null && reqHeaders.length>0){
				get.setHeaders(reqHeaders);
			}
			httpResponse  = client.execute(get,this.httpContext);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			//正常
			if (responseCode == OK) {
				res = dealResponse(httpResponse,charset,responseCode);
			//HttpStatus：状态码类（如：301、302、303、404、500、200等等，这里判断是否为30开头的跳转）
			} else if((responseCode+"").indexOf("30")!=-1){
				Header[] headers = httpResponse.getHeaders("location");
    	    	String newUrl = headers[0].getValue();
    	    	location=newUrl;
    	    	//未带有域时（比如：http），取host拼接起来
    	    	if(!newUrl.startsWith("http://") && !newUrl.startsWith("https://")){
    	    		Header[] postHeaders = get.getHeaders("Host");
    	    		if(postHeaders!=null && postHeaders.length>0){
    	    			newUrl = "http://"+postHeaders[0].getValue()+newUrl;
    	    		}
    	    	}
    	    	HttpGet newGet = new HttpGet(newUrl);
    			for(Header header:reqHeaders){
	    			if(header.getName().equals("User-Agent")){
	    				newGet.setHeader(header);
	    			}
	    			if(header.getName().equals("Referer")){
	    				newGet.setHeader(header);
	    			}
	    		}
    			EntityUtils.consume(httpResponse.getEntity());
    			HttpResponse responseNew = client.execute(newGet,this.httpContext);
    	    	res = dealResponse(responseNew,charset,responseCode);
			}else {
				resultEntity=httpResponse.getEntity();
				throw new HttpClientExecuteException(getCause(responseCode) + "\n" , responseCode);
			}
		//请求超时
		} catch(SocketTimeoutException e){
			responseCode=408;
		//连接超时
		} catch(ConnectTimeoutException e){
			responseCode=408;
		} catch (HttpClientExecuteException e){
			throw e;
		} catch (IOException e) {
			throw new HttpClientExecuteException(e.getMessage(), e, responseCode);
		}finally{
			try {if(resultEntity!=null){EntityUtils.consume(resultEntity);}}catch (IOException e) {}
			if(get!=null){
				get.abort();
			}
		}
		if(res==null){
			res=new Response(responseCode);
		}
		res.setLocation(location);
		return res;
    }
 
    /**获取返回的信息到Response*/
    private Response dealResponse(HttpResponse httpResponse,String charset,int statusCode) throws IllegalStateException, IOException, HttpClientExecuteException{
    	Header[] headers = httpResponse.getHeaders("Content-Type");
		String charsetName = null;
		//取返回的字符集
		for(Header h:headers){
			String headValue = h.getValue().toUpperCase();
			int position = -1;
			if((position = headValue.indexOf("CHARSET="))>=0){
				charsetName = headValue.substring(position+8);
				if((position = charsetName.indexOf(";"))>0){
					charsetName = charsetName.substring(0,position+1);
				}
			}
			if(charsetName!=null){
				break;
			}
		}
		//没取到，则用传来的字符集
		if(charset==null){
			charset = charsetName;
		}	
		//获取所有头部信息（包含cookie）
		List<Header> cookies = new ArrayList<Header>();
		Header headArr[]=httpResponse.getAllHeaders();
		for(int i=0;i<headArr.length;i++){
			Header t_head=headArr[i];
			cookies.add(t_head);
		}
		//返回结果数据
		HttpEntity returnEntity = httpResponse.getEntity();
		String html = "";
		if (returnEntity != null) {
			BufferedReader br =null;
			InputStream stream=null;
			Reader br_gzip=null;
            try {
				stream = returnEntity.getContent();
	            if (null == stream) {
	                return new Response(statusCode);
	            }
	            //GZIP数据
	            if(returnEntity instanceof GzipDecompressingEntity){
	            	br_gzip = new InputStreamReader(stream, charset);
                    int dataLength = (int)returnEntity.getContentLength();
                    if (dataLength < 0) {
                    	dataLength = 4096;
                    }
                    CharArrayBuffer buffer = new CharArrayBuffer(dataLength);
                    char[] tmp = new char[gzipLength];
                    int l;
                    try{
	                    while((l = br_gzip.read(tmp)) != -1) {
	                        buffer.append(tmp, 0, l);
	                    }
                    }catch(EOFException e1){
                    	//有一个EOFException中断了
                    }
                    html=buffer.toString();
                //其他类型数据 
            	}else{
            		br = new BufferedReader(new InputStreamReader(stream, charset));
		            StringBuffer buf = new StringBuffer(64);
		            String line;
		            int count = 0;
		            while (null != (line = br.readLine())) {
		                buf.append(line);
		                if(count>0){
		                	buf.append("\n");
		                }	
		                count++;
		            }
		            html = buf.toString();
            	}
            } catch (IOException e) {
	        	throw new HttpClientExecuteException(e.getMessage(),e);
	        } finally {
	        	try{if(br!=null){br.close();}}catch(Exception e){}
        		try{if(stream!=null){stream.close();}}catch(Exception e){}
        		try{if(br_gzip!=null){br_gzip.close();}}catch(Exception e){}
	        	try{if(returnEntity!=null){EntityUtils.consume(returnEntity);}}catch(IOException e){throw new HttpClientExecuteException(e.getMessage(),e);}
	        }
	        if(!html.equals("")) {
	        	Response res = new Response(httpResponse.getStatusLine().getStatusCode(),html,cookies);	
	        	return res;
	        }
		}
		return new Response(statusCode);
    }
    
    //【错误】状态码解释
    public static String getCause(int statusCode){
        String cause = null;
        switch(statusCode){
            case NOT_MODIFIED:break;
            case BAD_REQUEST : cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";break;
            case NOT_AUTHORIZED : cause = "Authentication credentials were missing or incorrect.";break;
            case FORBIDDEN : cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";break;
            case NOT_FOUND : cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";break;
            case NOT_ACCEPTABLE : cause = "Returned by the Search API when an invalid format is specified in the request.";break;
            case INTERNAL_SERVER_ERROR : cause = "Something is broken.  Please post to the group so the administrator can investigate.";break;
            case BAD_GATEWAY : cause = "YouXi is down or being upgraded.";break;
            case SERVICE_UNAVAILABLE : cause = "Service Unavailable: The Youxi servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited."; break;
            default : cause = "";
        }
        return statusCode + ":" + cause;
    }
    
    /**get : 泛型获取对应的数据**/
    public Object get_stream(String url) throws Exception{
    	return get_stream(url,null);
    }
    /**get : （1）泛型获取对应的数据**/
	public Object get_stream(String url,Map<String,String> headMap) throws Exception{
    	HttpGet get_stream=null;
    	InputStream is=null;
    	ObjectInputStream ois=null;
    	Object result=null;
    	HttpEntity resultEntity=null;
    	try{
    		headMap=headMap!=null?headMap:new HashMap<String, String>();
    		Header headArr[] = new Header[headMap.size()];
        	Iterator<String> key= headMap.keySet().iterator();				//Key【头部名称】
        	int i=0;
        	while(key.hasNext()){
        		String keyStr=key.next();
        		headArr[i]=new BasicHeader(keyStr,headMap.get(keyStr));
        		i++;
        	}
        	get_stream = new HttpGet(url);
        	get_stream.setHeaders(headArr);
    		HttpResponse hr = hc.execute(get_stream,this.httpContext);
    		resultEntity=hr.getEntity();
    		is=resultEntity.getContent();
    		ois = new ObjectInputStream(is);  
    		result=ois.readObject();
    	}finally{
			if(ois!=null){try{ois.close();}catch(Exception e){}}
			if(is!=null){try{is.close();}catch(Exception e){}}
			if(resultEntity!=null){try{EntityUtils.consume(resultEntity);}catch (IOException e){}}
    		if(get_stream!=null){get_stream.abort();}
    	}
    	return result;
    }
    /**post : （1）post流*/
  	public String post_stream(String url,Object obj,String charSet){
  		HttpPost post=null;
  		InputStream in=null;
  		ByteArrayInputStream bInput = null;  
  		ObjectOutputStream out = null;
  		ByteArrayOutputStream bOut=null;
  		BufferedReader br=null;
  		HttpEntity resultEntity=null;
  		String result=null;
  		try {
  			post=new HttpPost(url);
  			post.setHeader("Content-Type", "application/octet-stream"); 
  			bOut = new ByteArrayOutputStream(1024);  
  			out = new ObjectOutputStream(bOut);  
  			out.writeObject(obj);
  			bOut.flush();
  			out.flush();
  			
  			bInput = new ByteArrayInputStream(bOut.toByteArray());  
  			AbstractHttpEntity re = new InputStreamEntity(bInput);	
  			post.setEntity(re);
  			HttpResponse response=hc.execute(post,this.httpContext);					//调用接口，并返回结果（流）
  			resultEntity = response.getEntity();
  			in = resultEntity.getContent();
  			br = new BufferedReader(new InputStreamReader(in, charSet));
  			StringBuffer sb=new StringBuffer();
  			String line;
  			int count = 0;
            while ((line = br.readLine())!=null) {
                sb.append(line);
                if(count>0){
                	sb.append("\n");
                }	
                count++;
            }
            result=sb.toString();
  		}catch(Exception e){
  			logger.error(e.getMessage(),e);
  		}finally{
			if(br!=null){try{br.close();}catch(Exception e1){}}
			if(bInput!=null){try{bInput.close();}catch(Exception e1){}}
			if(out!=null){try{out.close();}catch(Exception e1){}}
			if(bOut!=null){try{bOut.close();}catch(Exception e1){}}
			if(in!=null){try{in.close();}catch(Exception e1){}}
			if(resultEntity!=null){try{EntityUtils.consume(resultEntity);}catch (IOException e){}}
  			if(post!=null){post.abort();}
  		}
  		return result;
  	}
  	
  	/**post : （2）post流*/
	public Object post_stream(String url,Object obj){
		HttpPost post=null;
  		InputStream in=null;
  		ByteArrayInputStream bInput = null;  
  		ObjectOutputStream out = null;
  		ByteArrayOutputStream bOut=null;
  		ObjectInputStream ois=null;
  		HttpEntity resultEntity=null;
  		Object result=null;
  		try {
  			post=new HttpPost(url);
  			post.setHeader("Content-Type", "application/octet-stream"); 
  			bOut = new ByteArrayOutputStream(1024);  
  			out = new ObjectOutputStream(bOut);  
  			out.writeObject(obj);
  			bOut.flush();
  			out.flush();
  			
  			bInput = new ByteArrayInputStream(bOut.toByteArray());  
  			AbstractHttpEntity re = new InputStreamEntity(bInput);	
  			post.setEntity(re);
  			HttpResponse response=hc.execute(post,this.httpContext);					//调用接口，并返回结果（流）
  			resultEntity = response.getEntity();
  			in = resultEntity.getContent();
    		ois = new ObjectInputStream(in);  
    		result=ois.readObject();
  		}catch(StreamCorruptedException e){
	  		//
  		}catch(Exception e){
  			logger.error(e.getMessage(),e);
  		}finally{
			if(ois!=null){try{ois.close();}catch(Exception e1){}}
			if(bInput!=null){try{bInput.close();}catch(Exception e1){}}
			if(out!=null){try{out.close();}catch(Exception e1){}}
			if(bOut!=null){try{bOut.close();}catch(Exception e1){}}
			if(in!=null){try{in.close();}catch(Exception e1){}}
			if(resultEntity!=null){try{EntityUtils.consume(resultEntity);}catch (IOException e){}}
  			if(post!=null){post.abort();}
  		}
  		return result;
  	}
	/**post : （3）post流*/
	public Object post_stream(String url,Map<String,String> paramMap,Map<String,String> headMap,String charset){
		HttpPost post=null;
  		InputStream in=null;
  		ObjectInputStream ois=null;
  		HttpEntity resultEntity=null;
  		Object result=null;
  		try {
  			UrlEncodedFormEntity reqEntity = null;
  			headMap=headMap!=null?headMap:new HashMap<String, String>();
  			int headLength=headMap!=null?headMap.size():0;
  	    	Header headArr[] = new Header[headLength];
  	    	List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>();
  	    	String charset_t="UTF-8";
  	    	if(charset!=null){
  	    		charset_t=charset;
  	    	}
			//如果没有参数
			if(paramMap!=null){
	    		Iterator<String> key= paramMap.keySet().iterator();									//Key【参数名称】
	    		while(key.hasNext()){
	    			String t_key=key.next();
	    			formParams.add(new BasicNameValuePair(t_key,String.valueOf(paramMap.get(t_key))));
	    		}
    		}
			Iterator<String> key= headMap.keySet().iterator();											//Key【头部名称】
	    	int i=0;
	    	while(key.hasNext()){
	    		String t_key=key.next();
	    		headArr[i]=new BasicHeader(t_key,(String) headMap.get(t_key));
	    		i++;
	    	}
			reqEntity = new UrlEncodedFormEntity(formParams,charset_t);
  			
			post=new HttpPost(url);
  			post.setHeaders(headArr); 
  			post.setEntity(reqEntity);
  			HttpResponse response=hc.execute(post,this.httpContext);					//调用接口，并返回结果（流）
  			resultEntity = response.getEntity();
  			in = resultEntity.getContent();
    		ois = new ObjectInputStream(in);  
    		result=ois.readObject();
  		}catch(StreamCorruptedException e){
  			//
  		}catch(Exception e){
  			logger.error(e.getMessage(),e);
  		}finally{
			if(ois!=null){try{ois.close();}catch(Exception e1){}}
			if(in!=null){try{in.close();}catch(Exception e1){}}
			if(resultEntity!=null){try{EntityUtils.consume(resultEntity);}catch (IOException e){}}
  			if(post!=null){post.abort();}
  		}
  		return result;
  	}
	/**get：（1）字节数组**/
	public byte[] get_byte(String url,Map<String,String> headMap){
		HttpGet get=null;
		HttpEntity resultEntity=null;
		InputStream is=null;
		ByteArrayOutputStream baos=null;
		byte byteArr[]=null;
		try{
			headMap=headMap!=null?headMap:new HashMap<String, String>();
			Header headArr[]=new Header[headMap.size()];
			int i=0;
			for(Entry<String, String> entry:headMap.entrySet()){
				headArr[i]=new BasicHeader(entry.getKey(),entry.getValue());
				i++;
			}
			get = new HttpGet(url);
			get.setHeaders(headArr);
			HttpResponse response = hc.execute(get,this.httpContext);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				throw new IllegalArgumentException("请求失败，响应状态码：" + response.getStatusLine().getStatusCode());
			}
			resultEntity = response.getEntity();
			is = resultEntity.getContent();
			baos = new ByteArrayOutputStream();
			byte[] buff = new byte[100];
			int rc = 0;
			while((rc = is.read(buff, 0, 100)) > 0){
				baos.write(buff, 0, rc);
			}
			byteArr=baos.toByteArray();
		}catch(Exception e){
			throw new RuntimeException(e.getMessage());
		}finally{
			if(baos!=null){try{baos.close();}catch(Exception e1){}}
			if(is!=null){try{is.close();}catch(Exception e1){}}
			if(resultEntity!=null){try{EntityUtils.consume(resultEntity);}catch (IOException e){}}
  			if(get!=null){get.abort();}
		}
		return byteArr;
	}

	//加入一对多数据
	public void put(Map<String, Object> paramListMap,String key,String value){
		List<Object> list=(List<Object>) paramListMap.get(key);
		if(list==null){
			list=new ArrayList<Object>();
		}
		list.add(value);
		paramListMap.put(key, list);
	}
}