package com.example.nutriscan.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextHelperTest {

    @Test
    public void normalizeRemovesExtraSpacesAndIgnoresCase() {
        assertEquals("arroz com atum", TextHelper.normalize("  Arroz   com ATUM "));
    }

    @Test
    public void normalizeRemovesDiacritics() {
        assertEquals("cafe com leite", TextHelper.normalize("Café COM leite"));
    }

    @Test
    public void normalizeHandlesNull() {
        assertEquals("", TextHelper.normalize(null));
    }
}
