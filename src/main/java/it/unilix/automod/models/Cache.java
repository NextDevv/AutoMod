package it.unilix.automod.models;

import lombok.Getter;

public record Cache(String message, String censored, boolean toxic) {
}
