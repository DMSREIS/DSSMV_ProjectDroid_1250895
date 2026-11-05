package com.example.nutriscan.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.nutriscan.R;

import java.util.Locale;

public class SliderInput extends LinearLayout {

    public interface OnValueChangeListener {
        void onValueChanged(double value);
    }

    private final SeekBar seekBar;
    private final EditText editText;
    private double max = 100;
    private double step = 1;
    private boolean updating = false;
    private OnValueChangeListener listener;

    public SliderInput(Context context) {
        this(context, null);
    }

    public SliderInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_slider_input, this, true);

        seekBar = findViewById(R.id.sliderSeek);
        editText = findViewById(R.id.sliderValue);
        seekBar.setMax(100);
        setText("0");

        // Arrastar o slider atualiza o número
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) {
                    setText(formatValue(getValue()));
                    notifyChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) { }

            @Override
            public void onStopTrackingTouch(SeekBar bar) { }
        });

        // Escrever um número atualiza o slider
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                onTextEdited(s.toString());
            }
        });

        // Ao sair do campo, o texto passa a mostrar o valor real do slider
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                normalizeText();
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                normalizeText();
            }
            return false; // deixa o teclado fechar
        });
    }

    // Valores de 0 até max, em passos de step
    public void setup(double max, double step) {
        this.max = max;
        this.step = step;
        seekBar.setMax((int) Math.round(max / step));
        setText(formatValue(getValue()));
    }

    public double getValue() {
        return Math.round(seekBar.getProgress() * step * 100.0) / 100.0;
    }

    public void setValue(double value) {
        double v = Math.max(0, Math.min(max, value));
        setProgressFromValue(v);
        setText(formatValue(getValue()));
        notifyChanged();
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public void setError(CharSequence error) {
        editText.setError(error);
    }

    // Texto lido pelo TalkBack
    public void setLabel(CharSequence label) {
        seekBar.setContentDescription(label);
        editText.setContentDescription(label);
    }

    private void onTextEdited(String raw) {
        if (updating) {
            return;
        }
        String s = raw.trim().replace(',', '.');
        if (s.isEmpty()) {
            setProgressFromValue(0);
            notifyChanged();
            return;
        }
        double parsed;
        try {
            parsed = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return; // por exemplo "." sozinho: espera pelo resto
        }
        if (parsed > max) {
            setText(formatValue(max)); // acima do máximo: fica no máximo
            setProgressFromValue(max);
        } else {
            setProgressFromValue(parsed);
        }
        notifyChanged();
    }

    private void setProgressFromValue(double value) {
        int progress = (int) Math.round(value / step);
        progress = Math.max(0, Math.min(seekBar.getMax(), progress));
        seekBar.setProgress(progress);
    }

    private void normalizeText() {
        setText(formatValue(getValue()));
    }

    private void setText(String text) {
        updating = true;
        editText.setText(text);
        editText.setSelection(editText.getText().length());
        updating = false;
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onValueChanged(getValue());
        }
    }

    private String formatValue(double v) {
        double rounded = Math.round(v * 100.0) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.format(Locale.US, "%.0f", rounded);
        }
        return String.format(Locale.US, "%.1f", rounded);
    }

    // Vários SliderInput no mesmo ecrã repetem os ids internos: guarda só o valor
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super", super.onSaveInstanceState());
        bundle.putDouble("value", getValue());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable("super"));
            setValue(bundle.getDouble("value"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}