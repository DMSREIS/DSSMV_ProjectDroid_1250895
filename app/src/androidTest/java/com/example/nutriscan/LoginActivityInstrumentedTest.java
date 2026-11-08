package com.example.nutriscan;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class LoginActivityInstrumentedTest {

    @Test
    public void invalidEmailShowsValidationMessage() {
        // Garante que o ecrã de autenticação aparece em vez de abrir diretamente o menu.
        FirebaseAuth.getInstance().signOut();

        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etEmail))
                    .perform(replaceText("email-invalido"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(replaceText("123456"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etEmail)).check(matches(hasErrorText("Email inválido")));
        }
    }

    @Test
    public void shortPasswordShowsValidationMessage() {
        FirebaseAuth.getInstance().signOut();

        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etEmail))
                    .perform(replaceText("aluno@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword))
                    .perform(replaceText("123"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.etPassword)).check(matches(hasErrorText("Mínimo 6 caracteres")));
        }
    }
}
