package vn.edu.bookstore.legacy.adapter.config;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.bookstore.legacy.adapter.soap.SoapLoggingHandler;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogPortType;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogService;

@Configuration
public class LegacySoapClientConfig {

    @Bean
    BookCatalogPortType bookCatalogPort(LegacySoapProperties props) {
        // TH1-4: tạo proxy từ WSDL nằm trong classpath
        URL wsdl = LegacySoapClientConfig.class.getResource("/wsdl/BookCatalogService.wsdl");
        BookCatalogPortType port = new BookCatalogService(wsdl).getBookCatalogPort();

        Map<String, Object> ctx = ((BindingProvider) port).getRequestContext();
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, props.endpoint());
        ctx.put("com.sun.xml.ws.connect.timeout", (int) props.connectTimeout().toMillis());
        ctx.put("com.sun.xml.ws.request.timeout", (int) props.readTimeout().toMillis());

        // TH1-5: gắn handler ghi log thông điệp SOAP vào/ra
        if (props.logMessages()) {
            @SuppressWarnings("rawtypes")
            List<Handler> chain = new ArrayList<>(((BindingProvider) port).getBinding().getHandlerChain());
            chain.add(new SoapLoggingHandler());
            ((BindingProvider) port).getBinding().setHandlerChain(chain);
        }
        return port;
    }
}
