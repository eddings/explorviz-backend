package net.explorviz.landscape.repository.persistence.mongo;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.exceptions.DocumentSerializationException;
import javax.inject.Inject;
import net.explorviz.shared.landscape.model.landscape.Landscape;

/**
 * Helper class for de-/serializing landscapes from/to json api.
 *
 */
public class LandscapeSerializationHelper {


  @Inject
  private ResourceConverter jsonApiConverter;


  /**
   * Serializes a landscape to a json api string.
   *
   * @throws DocumentSerializationException if the landscape could not be parsed.
   */
  public String serialize(final Landscape l) throws DocumentSerializationException {
    final JSONAPIDocument<Landscape> landscapeDoc = new JSONAPIDocument<>(l);
    final byte[] landscapeBytes = this.jsonApiConverter.writeDocument(landscapeDoc);
    return new String(landscapeBytes);

  }

  /**
   * Deserializes a json-api string to a {@link Landscape} object.
   *
   * @param jsonApi the json api string representing a landscape
   * @return the landscape
   * @throws DocumentSerializationException if the given string can't be deserialized to a landscape
   */
  public Landscape deserialize(final String jsonApi) throws DocumentSerializationException {
    final JSONAPIDocument<Landscape> landscapeDoc =
        this.jsonApiConverter.readDocument(jsonApi.getBytes(), Landscape.class);

    return landscapeDoc.get();
  }

}
