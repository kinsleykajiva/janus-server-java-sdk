package io.github.kinsleykajiva.rest.admin;

import io.github.kinsleykajiva.utils.JanusPlugins;

public record TokenRecord(String token, JanusPlugins[] allowedPlugins) {
}
