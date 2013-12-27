package org.elasticsearch.helpers.fields;

import org.elasticsearch.common.settings.Settings;

public final class SettingsHelper
{
    private static Settings _settings;
    
    public static Settings GetSettings()
    {
        return _settings;
    }
    
    public static void SetSettings(Settings settings)
    {
        _settings = settings;
    }
}
