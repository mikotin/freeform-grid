package com.vaadin.demo.grid;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.HorizontalLayout;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("valo")
@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
public class MyUI extends UI {

    HorizontalLayout split;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        split = new HorizontalLayout();
        split.setSizeFull();
        split.addComponent(new MySqlEditor(MySqlEditor.ConnectionType.SQLCONTAINER));
        split.addComponent(new MySqlEditor(MySqlEditor.ConnectionType.PLAINSQL));
        setContent(split);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
