/**
 * Copyright 2021 rahulstch
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package docs.la_creme_des_cremes_docs.login;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * This helper class handles the showing and masking password text.
 * Currently it uses a separate {@link TextField} which is created
 * during the instantiating this class and added to the same location
 * as the target password field. The password masking and unmasking is
 * controlled by a imageview. The imageview image is also change by this
 * class.
 */
public class PasswordVisibilityHelper {

    private Pane parent;
    private PasswordField notVisibleField;
    private TextField visibleField;
    private ImageView toggleButton;
    private Image visibleImage;
    private Image notVisibleImage;
    private ChangeListenerWrapper<Boolean> focusChangeListener;

    private BooleanProperty visibility = new SimpleBooleanProperty(this, "visibility", false);

    public PasswordVisibilityHelper(PasswordField field, ImageView toggleButton) {

        this.notVisibleField = field;
        this.toggleButton = toggleButton;
        this.visibleField = createTextFieldAs(field);
        this.visibleImage = loadImage("visibility_white.png");
        this.notVisibleImage = loadImage("visibility_off_white.png");
        this.focusChangeListener = new ChangeListenerWrapper<>();
        visibleField.focusedProperty().addListener(focusChangeListener);
        notVisibleField.focusedProperty().addListener(focusChangeListener);

        this.notVisibleField.visibleProperty().bind(visibility.not());
        this.visibleField.visibleProperty().bind(visibility);

        this.notVisibleField.textProperty().bindBidirectional(this.visibleField.textProperty());
    }

    public BooleanProperty visibilityProperty() {
        return visibility;
    }

    public boolean isVisible() {
        return visibility.get();
    }

    public void changeVisibility(boolean visible) {
        boolean focused = isVisible() ? visibleField.focusedProperty().get()
                : notVisibleField.focusedProperty().get();
        visibility.set(visible);
        if (visible) {
            if (focused) visibleField.requestFocus();
            toggleButton.setImage(notVisibleImage);
        } else {
            if (focused) notVisibleField.requestFocus();
            toggleButton.setImage(visibleImage);
        }
    }

    public void toggleVisibility() {
        changeVisibility(!isVisible());
    }

    public TextField currentField() {
        return isVisible() ? visibleField : notVisibleField;
    }

    public TextField getVisibleField() {
        return visibleField;
    }

    public PasswordField getNotVisibleField() {
        return notVisibleField;
    }

    public void addFocusChangeListener(ChangeListener<Boolean> listener) {
        focusChangeListener.wrap(listener);
    }

    private TextField createTextFieldAs(PasswordField field) {
        double x = field.getLayoutX();
        double y = field.getLayoutY();
        double width = field.getPrefWidth();
        double height = field.getPrefHeight();

        TextField textField = new TextField();
        textField.setLayoutX(x);
        textField.setLayoutY(y);
        textField.setPrefHeight(height);
        textField.setPrefWidth(width);
        textField.setId(field.getId());
        textField.setPromptText(field.getPromptText());
        textField.getStyleClass().addAll(field.getStyleClass());

        return textField;
    }

    private Image loadImage(String name) {
        return new Image(loginController.class.getResource(name).toExternalForm());
    }

    private class ChangeListenerWrapper<T> implements ChangeListener<T> {

        ChangeListener<T> wrapped = null;

        public void wrap(ChangeListener<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            if (null != wrapped) wrapped.changed(observable, oldValue, newValue);
        }
    }
}
