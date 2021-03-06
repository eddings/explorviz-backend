package net.explorviz.landscape.server.exceptions.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.server.ParamException.PathParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exeception mapping for path parameter exceptions.
 *
 */
public class PathParamExceptionMapper implements ExceptionMapper<PathParamException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathParamExceptionMapper.class);

  private static final int HTTP_CLIENT_ERROR_STATUS = 400;

  @Override
  public Response toResponse(final PathParamException exception) {

    final List<Map<String, Object>> array = new ArrayList<>();

    final Map<String, Object> errorObject = new HashMap<>();
    errorObject.put("status", String.valueOf(HTTP_CLIENT_ERROR_STATUS));
    errorObject.put("title", "Invalid path parameter(s)");
    errorObject.put("detail", exception.getCause().toString());

    array.add(errorObject);

    final Map<String, Object> errorsArray = new HashMap<>();
    errorsArray.put("errors", array.toArray());

    String returnMessage = "";

    final ObjectMapper mapper = new ObjectMapper();

    try {
      returnMessage = mapper.writeValueAsString(errorsArray);
    } catch (final JsonProcessingException e) {
      LOGGER.debug(e.getMessage());
    }

    return Response.status(HTTP_CLIENT_ERROR_STATUS).header("Content-Type", "application/json")
        .entity(returnMessage).build();
  }
}
