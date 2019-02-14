package net.explorviz.security.server.providers;

import com.github.jasminb.jsonapi.DeserializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import net.explorviz.shared.security.model.User;
import net.explorviz.shared.security.model.roles.Role;
import net.explorviz.shared.security.model.settings.BooleanSettingDescriptor;
import net.explorviz.shared.security.model.settings.NumericSettingDescriptor;
import net.explorviz.shared.security.model.settings.StringSettingDescriptor;
import net.explorviz.shared.security.model.settings.UserSettings;
import org.glassfish.hk2.api.Factory;

/**
 * Factory for creating resource converts for JSON conversion.
 */
public class ResourceConverterFactory implements Factory<ResourceConverter> {
  private final ResourceConverter converter;

  /**
   * Creates a new factory.
   */
  public ResourceConverterFactory() {
    final ResourceConverter resourceConverter = new ResourceConverter();

    resourceConverter.registerType(User.class);
    resourceConverter.registerType(Role.class);
    resourceConverter.registerType(UserSettings.class);
    resourceConverter.registerType(BooleanSettingDescriptor.class);
    resourceConverter.registerType(NumericSettingDescriptor.class);
    resourceConverter.isRegisteredType(StringSettingDescriptor.class);
    // resourceConverter.registerType(SettingDescriptor.class);


    this.converter = resourceConverter;
    this.converter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);

    // Clients send records with no id, therefore allow this feature
    this.converter.disableDeserializationOption(DeserializationFeature.REQUIRE_RESOURCE_ID);
  }

  @Override
  public void dispose(final ResourceConverter arg0) {
    // Nothing to dispose
  }

  @Override
  public ResourceConverter provide() {
    return this.converter;
  }
}
