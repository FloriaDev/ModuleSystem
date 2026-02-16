package ru.feeland.modulesystem.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import org.bukkit.command.CommandSender;
import org.codehaus.plexus.util.StringUtils;
import ru.feeland.modulesystem.BaseModuleSystem;
import ru.feeland.modulesystem.config.impl.BaseMainConfig;
import ru.feeland.modulesystem.logger.Logger;
import ru.feeland.modulesystem.responce.ApiExceptionResponse;
import ru.feeland.modulesystem.service.BaseService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HttpService extends BaseService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static String API_URL;
    private final String API_TOKEN = getPlugin().getConfig(BaseMainConfig.class).getString("apiToken");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public HttpService(BaseModuleSystem plugin) {
        super(plugin);
        API_URL = getPlugin().getConfig(BaseMainConfig.class).getString("apiUrl");
    }

    public <T> HttpResponseContent<T> get(String uri, Object entity, Class<T> responseClass) {
        return exchange(uri, "GET", entity, responseClass);
    }

    public <T> HttpResponseContent<T> post(String uri, Object entity, Class<T> responseClass) {
        return exchange(uri, "POST", entity, responseClass);
    }

    public <T> HttpResponseContent<T> put(String uri, Object entity, Class<T> responseClass) {
        return exchange(uri, "PUT", entity, responseClass);
    }

    public <T> HttpResponseContent<T> delete(String uri, Object entity, Class<T> responseClass) {
        return exchange(uri, "DELETE", entity, responseClass);
    }

    private <T> HttpResponseContent<T> exchange(String uri, String method, Object entity, Class<T> responseClass) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", API_TOKEN)
                .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(entity)))
                .build();

            CompletableFuture<HttpResponse<String>> futureResponse = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            HttpResponse<String> response = futureResponse.get();

            if (response.statusCode() != 200) {
                return new HttpResponseContent<>(
                    objectMapper.readValue(response.body(), ApiExceptionResponse.class),
                    response.statusCode()
                );
            }

            if (StringUtils.isNotEmpty(response.body())) {
                return new HttpResponseContent<>(
                    objectMapper.readValue(response.body(), responseClass),
                    response.statusCode()
                );
            } else {
                return new HttpResponseContent<>(response.statusCode());
            }

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            return new HttpResponseContent<>(e.getCause() != null ? e.getCause() : e);
        } catch (Exception e) {
            return new HttpResponseContent<>(e);
        }
    }

    public void failure(CommandSender sender, HttpResponseContent content) {
        if (content.getException() != null) {
            return;
        }

        if (content.getApiExceptionResponse() != null) {
            Logger.warn().log("API Error: Code {}, Message: {} Map: {}",
                content.getApiExceptionResponse().getCode(),
                content.getApiExceptionResponse().getMessage(),
                content.getApiExceptionResponse().getValidationErrors()
            );

            sender.sendMessage("Код ошибки: %d. %s".formatted(
                content.getApiExceptionResponse().getCode(),
                content.getApiExceptionResponse().getMessage()
            ));
        }
    }

    public static class HttpResponseContent<T> {
        protected final T body;
        protected final ApiExceptionResponse apiExceptionResponse;
        protected final Integer code;
        protected final Throwable exception;

        public HttpResponseContent(T body, Integer code) {
            this.body = body;
            this.code = code;
            this.exception = null;
            this.apiExceptionResponse = null;
        }

        public HttpResponseContent(ApiExceptionResponse apiExceptionResponse, Integer code) {
            this.apiExceptionResponse = apiExceptionResponse;
            this.body = null;
            this.code = code;
            this.exception = null;
        }

        public HttpResponseContent(Throwable exception) {
            this.body = null;
            this.code = null;
            this.exception = exception;
            this.apiExceptionResponse = null;
        }

        public HttpResponseContent(Integer code) {
            this.body = null;
            this.code = code;
            this.exception = null;
            this.apiExceptionResponse = null;
        }

        public ApiExceptionResponse getApiExceptionResponse() {
            return apiExceptionResponse;
        }

        public T getBody() {
            return body;
        }

        public Integer getCode() {
            return code;
        }

        public Throwable getException() {
            return exception;
        }

        public boolean isSuccess() {
            return getCode() != null && getCode() == 200;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("body", body)
                .add("apiExceptionResponse", apiExceptionResponse)
                .add("code", code)
                .add("exception", exception)
                .toString();
        }
    }
}