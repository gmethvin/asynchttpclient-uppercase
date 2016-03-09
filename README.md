# AsyncHttpClient upppercase HTTP issue

This is a small project to demonstrate an issue with AsyncHttpClient 1.8. When requesting a URI in uppercase such as `HTTP://EXAMPLE.COM`, AHC appears to send it as HTTPS rather than HTTP. I get error messages like:

```
[info] 14:09:45.405 DEBUG - com.ning.http.client.providers.netty.NettyAsyncHttpProvider [New I/O worker #2] - Unexpected I/O exception on channel [id: 0x79effb84, /10.0.1.41:65145 => EXAMPLE.COM/93.184.216.34:443]
```
