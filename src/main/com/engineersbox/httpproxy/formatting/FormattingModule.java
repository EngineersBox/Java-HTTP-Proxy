package com.engineersbox.httpproxy.formatting;

import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.content.ContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.HTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class FormattingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<BaseHTTPFormatter<HTTPRequestStartLine>>(){})
            .to(new TypeLiteral<HTTPFormatter<HTTPRequestStartLine>>(){});
        bind(new TypeLiteral<BaseHTTPFormatter<HTTPResponseStartLine>>(){})
            .to(new TypeLiteral<HTTPFormatter<HTTPResponseStartLine>>(){});
        bind(BaseContentFormatter.class)
            .to(ContentFormatter.class);
    }
}
