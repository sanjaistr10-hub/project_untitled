package com.projectuntitled.community;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void derivesDnsSafeSlugsFromNames() {
        assertThat(Slugs.fromName("Arjun's IFVG Room!")).isEqualTo("arjuns-ifvg-room");
        assertThat(Slugs.fromName("  Café Crème  ")).isEqualTo("cafe-creme");
        assertThat(Slugs.fromName("🔥🔥")).isEqualTo("community");
        assertThat(Slugs.fromName("AB")).isEqualTo("ab-community");
        assertThat(Slugs.fromName("x".repeat(60))).hasSize(Slugs.MAX_LENGTH);
    }

    @Test
    void rejectsInvalidAndReservedSlugs() {
        assertThat(Slugs.problem("ok-slug")).isEmpty();
        assertThat(Slugs.problem("ab")).isPresent();
        assertThat(Slugs.problem("-leading")).isPresent();
        assertThat(Slugs.problem("has_underscore")).isPresent();
        assertThat(Slugs.problem("app")).isPresent();
        assertThat(Slugs.problem("www")).isPresent();
    }
}
