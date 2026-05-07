/*
 * This file is part of faker - https://github.com/o1seth/faker
 * Copyright (C) 2024 o1seth
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.faker.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import net.java.faker.Proxy;
import net.java.faker.WinRedirect;
import net.java.faker.auth.Account;
import net.java.faker.auth.MicrosoftAccount;
import net.java.faker.proxy.dhcp.Dhcp;
import net.java.faker.proxy.event.*;
import net.java.faker.proxy.session.ProxyConnection;
import net.java.faker.save.Config;
import net.java.faker.ui.elements.NetworkAdapterComboBox;
import net.java.faker.ui.tab.AddAccountPopup;
import net.java.faker.util.Sys;
import net.java.faker.util.TFunction;
import net.java.faker.util.Util;
import net.java.faker.util.logging.Logger;
import net.java.faker.util.network.NetworkInterface;
import net.java.faker.util.network.NetworkUtil;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class AmethystWindow extends JFrame {
    private static AmethystWindow INSTANCE;
    private static final String APP_NAME = "Faker Custom UI fork by anydefax";

    private Color BACKGROUND;
    private Color SURFACE;
    private Color SURFACE_HOVER;
    private Color STROKE;
    private Color AMETHYST;
    private Color AMETHYST_SOFT;
    private Color TEXT;
    private Color MUTED;
    private Color GOOD;
    private Color WARN;
    private Color CONTROL;
    private Color LIST;
    private Color SELECTION_TEXT;
    private Color GRADIENT_START;
    private Color GRADIENT_END;
    private Color GLOW_PRIMARY;
    private Color GLOW_SECONDARY;
    private Color GLASS_LINE;
    private Theme theme;

    private final AnimatedPages pages = new AnimatedPages();
    private final List<NavButton> navButtons = new ArrayList<>();
    private final Timer pulseTimer;
    private float pulse;
    private DashboardPage dashboardPage;
    private AdvancedPage advancedPage;
    private AccountsPage accountsPage;
    private DhcpPage dhcpPage;
    private SettingsPage settingsPage;
    private PopupMenu trayMenu;
    private TrayIcon trayIcon;

    public static synchronized AmethystWindow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AmethystWindow();
        } else {
            INSTANCE.setVisible(true);
        }
        return INSTANCE;
    }

    private AmethystWindow() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> showException(e));
        setTheme(Theme.fromId(Proxy.getConfig().uiTheme.get()), false);
        setLookAndFeel();
        initWindow();
        initLayout();
        initTray();
        ToolTipManager.sharedInstance().setInitialDelay(120);
        ToolTipManager.sharedInstance().setDismissDelay(10_000);

        pulseTimer = new Timer(42, e -> {
            pulse += 0.035f;
            repaint();
        });
        pulseTimer.start();

        setVisible(true);
    }

    private void setLookAndFeel() {
        try {
            FlatDarkLaf.setup();
            applyThemeDefaults();
        } catch (Throwable t) {
            Logger.error("Failed set look and feel", t);
        }
    }

    private void applyThemeDefaults() {
        try {
            UIManager.put("Component.arc", 14);
            UIManager.put("Button.arc", 14);
            UIManager.put("TextComponent.arc", 14);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", AMETHYST);
            UIManager.put("ComboBox.buttonArrowColor", AMETHYST_SOFT);
            UIManager.put("ComboBox.background", CONTROL);
            UIManager.put("ComboBox.foreground", TEXT);
            UIManager.put("TextField.background", CONTROL);
            UIManager.put("TextField.foreground", TEXT);
            UIManager.put("TextField.caretForeground", AMETHYST_SOFT);
            UIManager.put("List.background", LIST);
            UIManager.put("List.foreground", TEXT);
            UIManager.put("List.selectionBackground", AMETHYST);
            UIManager.put("List.selectionForeground", SELECTION_TEXT);
            UIManager.put("CheckBox.icon.focusWidth", 0);
            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
        } catch (Throwable t) {
            Logger.error("Failed apply theme defaults", t);
        }
    }

    private void setTheme(Theme nextTheme, boolean updateTree) {
        theme = nextTheme == null ? Theme.PREMIUM_PRESTIGE : nextTheme;
        if (theme == Theme.CUSTOM) {
            applyCustomTheme();
        } else {
            applyPresetTheme(theme);
        }
        if (updateTree) {
            applyThemeDefaults();
            setIconImages(List.of(createAmethystIcon(16), createAmethystIcon(32), createAmethystIcon(64)));
            SwingUtilities.updateComponentTreeUI(this);
            applyComponentTheme(getContentPane());
            repaint();
        }
    }

    private void applyPresetTheme(Theme preset) {
        BACKGROUND = theme.background;
        SURFACE = theme.surface;
        SURFACE_HOVER = theme.surfaceHover;
        STROKE = theme.stroke;
        AMETHYST = theme.accent;
        AMETHYST_SOFT = theme.accentSoft;
        TEXT = theme.text;
        MUTED = theme.muted;
        GOOD = theme.good;
        WARN = theme.warn;
        CONTROL = preset.control;
        LIST = preset.list;
        SELECTION_TEXT = preset.selectionText;
        GRADIENT_START = preset.gradientStart;
        GRADIENT_END = preset.gradientEnd;
        GLOW_PRIMARY = preset.glowPrimary;
        GLOW_SECONDARY = preset.glowSecondary;
        GLASS_LINE = preset.glassLine;
    }

    private void applyCustomTheme() {
        Config config = Proxy.getConfig();
        BACKGROUND = parseColor(config.uiCustomBackground.get(), Theme.PREMIUM_PRESTIGE.background);
        SURFACE = withAlpha(parseColor(config.uiCustomSurface.get(), Theme.PREMIUM_PRESTIGE.surface), 220);
        SURFACE_HOVER = withAlpha(parseColor(config.uiCustomSurface.get(), Theme.PREMIUM_PRESTIGE.surfaceHover).brighter(), 238);
        STROKE = withAlpha(parseColor(config.uiCustomAccent.get(), Theme.PREMIUM_PRESTIGE.stroke), 108);
        AMETHYST = parseColor(config.uiCustomAccent.get(), Theme.PREMIUM_PRESTIGE.accent);
        AMETHYST_SOFT = parseColor(config.uiCustomAccentSoft.get(), Theme.PREMIUM_PRESTIGE.accentSoft);
        TEXT = parseColor(config.uiCustomText.get(), Theme.PREMIUM_PRESTIGE.text);
        MUTED = parseColor(config.uiCustomMuted.get(), Theme.PREMIUM_PRESTIGE.muted);
        GOOD = Theme.PREMIUM_PRESTIGE.good;
        WARN = Theme.PREMIUM_PRESTIGE.warn;
        CONTROL = mix(SURFACE, BACKGROUND, 0.18f);
        LIST = mix(SURFACE, BACKGROUND, 0.32f);
        SELECTION_TEXT = brightness(AMETHYST) > 150 ? Color.BLACK : Color.WHITE;
        GRADIENT_START = mix(SURFACE, BACKGROUND, 0.2f);
        GRADIENT_END = BACKGROUND.darker();
        GLOW_PRIMARY = AMETHYST.darker();
        GLOW_SECONDARY = AMETHYST_SOFT;
        GLASS_LINE = withAlpha(AMETHYST_SOFT, 10);
    }

    private void applyComponentTheme(Component component) {
        if (component instanceof JLabel label) {
            Color foreground = label.getForeground();
            if (foreground == null || isThemeTextColor(foreground)) {
                label.setForeground(TEXT);
            }
        } else if (component instanceof JTextField textField) {
            textField.setForeground(TEXT);
            textField.setBackground(CONTROL);
            textField.setCaretColor(AMETHYST_SOFT);
        } else if (component instanceof JComboBox<?> comboBox) {
            comboBox.setForeground(TEXT);
            comboBox.setBackground(CONTROL);
        } else if (component instanceof JList<?> list) {
            list.setForeground(TEXT);
            list.setBackground(LIST);
            list.setSelectionBackground(AMETHYST);
            list.setSelectionForeground(SELECTION_TEXT);
        } else if (component instanceof JScrollPane scrollPane) {
            scrollPane.getViewport().setBackground(LIST);
        } else if (component instanceof AbstractButton button) {
            button.setForeground(button instanceof AnimatedButton animated && animated.primary ? Color.WHITE : TEXT);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyComponentTheme(child);
            }
        }
    }

    private boolean isThemeTextColor(Color color) {
        for (Theme candidate : Theme.values()) {
            if (sameRgb(color, candidate.text) || sameRgb(color, candidate.muted) || sameRgb(color, candidate.accentSoft)) {
                return true;
            }
        }
        return sameRgb(color, Color.WHITE);
    }

    private boolean sameRgb(Color a, Color b) {
        return a.getRed() == b.getRed() && a.getGreen() == b.getGreen() && a.getBlue() == b.getBlue();
    }

    private void initWindow() {
        setTitle(APP_NAME + " " + Proxy.VERSION);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setIconImages(List.of(createAmethystIcon(16), createAmethystIcon(32), createAmethystIcon(64)));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                applyGuiState();
                Proxy.getConfig().save();
                if (!Proxy.isStarted() && !Dhcp.isStarted()) {
                    System.exit(0);
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                showTray();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                hideTray();
            }
        });
        setMinimumSize(new Dimension(980, 660));
        setSize(1060, 720);
        setLocationRelativeTo(null);
    }

    private void initLayout() {
        JPanel root = new AmethystRootPanel();
        root.setOpaque(false);
        root.setLayout(new BorderLayout(20, 0));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(root);

        GlassPanel side = new GlassPanel();
        side.setLayout(new BorderLayout());
        side.setPreferredSize(new Dimension(226, 10));
        side.setBorder(new EmptyBorder(20, 16, 16, 16));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Faker Custom UI");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel fork = new JLabel("fork by anydefax");
        fork.setForeground(MUTED);
        fork.setFont(fork.getFont().deriveFont(11f));
        brand.add(title);
        brand.add(Box.createVerticalStrut(3));
        brand.add(fork);
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(28, 0, 0, 0));
        addPage(nav, "dashboard", "Dashboard", dashboardPage = new DashboardPage());
        addPage(nav, "advanced", "Advanced", advancedPage = new AdvancedPage());
        addPage(nav, "accounts", "Accounts", accountsPage = new AccountsPage());
        if (Sys.isWindows()) {
            addPage(nav, "dhcp", "DHCP", dhcpPage = new DhcpPage());
        }
        addPage(nav, "settings", "Settings", settingsPage = new SettingsPage());
        side.add(nav, BorderLayout.CENTER);

        JLabel status = new JLabel(WinRedirect.isSupported() ? "WinRedirect ready" : "Portable mode");
        status.setForeground(MUTED);
        status.setBorder(new EmptyBorder(12, 2, 0, 2));
        side.add(status, BorderLayout.SOUTH);

        root.add(side, BorderLayout.WEST);
        root.add(pages, BorderLayout.CENTER);
        pages.setOpaque(false);
        showPage("dashboard");
    }

    private void addPage(JPanel nav, String id, String label, JPanel page) {
        pages.addPage(id, page);
        NavButton button = new NavButton(label);
        button.putClientProperty("pageId", id);
        button.addActionListener(e -> showPage(id));
        navButtons.add(button);
        nav.add(button);
        nav.add(Box.createVerticalStrut(8));
    }

    private void showPage(String id) {
        pages.showPage(id);
        for (NavButton button : navButtons) {
            button.setSelected(id.equals(button.getClientProperty("pageId")));
        }
    }

    private void initTray() {
        if (!SystemTray.isSupported()) return;
        ActionListener listener = e -> setVisible(true);
        trayMenu = new PopupMenu();
        MenuItem showButton = new MenuItem();
        I18n.link(showButton, "tray.show");
        showButton.addActionListener(listener);
        trayMenu.add(showButton);
        MenuItem exitButton = new MenuItem();
        I18n.link(exitButton, "tray.exit");
        exitButton.addActionListener(e -> {
            applyGuiState();
            Proxy.getConfig().save();
            System.exit(0);
        });
        trayMenu.add(exitButton);
        trayIcon = new TrayIcon(createAmethystIcon(16), APP_NAME, trayMenu);
        trayIcon.addActionListener(listener);
    }

    private void showTray() {
        try {
            if (trayIcon != null) SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            Logger.error(e.getMessage());
        }
    }

    private void hideTray() {
        if (trayIcon != null) SystemTray.getSystemTray().remove(trayIcon);
    }

    private void applyGuiState() {
        if (dashboardPage != null) dashboardPage.applyGuiState();
        if (advancedPage != null) advancedPage.applyGuiState();
        if (dhcpPage != null) dhcpPage.applyGuiState();
    }

    public void switchToClassic() {
        applyGuiState();
        Proxy.getConfig().uiShell.set("classic");
        Proxy.getConfig().save();
        hideTray();
        dispose();
        INSTANCE = null;
        Window.getInstance();
    }

    private Image createAmethystIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(19, 18, 28));
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);
        Path2D gem = new Path2D.Float();
        gem.moveTo(size * 0.50, size * 0.10);
        gem.lineTo(size * 0.84, size * 0.35);
        gem.lineTo(size * 0.70, size * 0.86);
        gem.lineTo(size * 0.30, size * 0.86);
        gem.lineTo(size * 0.16, size * 0.35);
        gem.closePath();
        g.setPaint(new GradientPaint(0, 0, AMETHYST_SOFT, size, size, AMETHYST.darker()));
        g.fill(gem);
        g.setColor(new Color(255, 255, 255, 90));
        g.drawLine((int) (size * 0.50), (int) (size * 0.12), (int) (size * 0.50), (int) (size * 0.84));
        g.drawLine((int) (size * 0.18), (int) (size * 0.36), (int) (size * 0.82), (int) (size * 0.36));
        g.dispose();
        return image;
    }

    public static void showException(final Throwable t) {
        Logger.error("Caught exception in thread " + Thread.currentThread().getName() + t);
        StringBuilder builder = new StringBuilder("An error occurred:\n");
        builder.append("[").append(t.getClass().getSimpleName()).append("] ").append(t.getMessage()).append("\n");
        for (StackTraceElement element : t.getStackTrace()) builder.append(element).append("\n");
        showError(builder.toString());
    }

    public static void showInfo(final String message) {
        showNotification(message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(final String message) {
        showNotification(message, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(final String message) {
        showNotification(message, JOptionPane.ERROR_MESSAGE);
    }

    public static void showNotification(final String message, final int type) {
        JOptionPane.showMessageDialog(AmethystWindow.getInstance(), message, APP_NAME, type);
    }

    private class DashboardPage extends Page {
        private final JComboBox<String> serverAddress;
        private final JComboBox<Account> accounts;
        private final JButton startButton;
        private final JButton pauseButton;
        private final JLabel statusLabel;
        private final JLabel endpointLabel;
        private final ClientCard leftClient;
        private final ClientCard rightClient;
        private ProxyConnection leftConnection;
        private ProxyConnection rightConnection;

        DashboardPage() {
            super("Dashboard", "Server control, account selection, client pairing.");

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            add(grid, BorderLayout.CENTER);

            GlassPanel controls = card();
            controls.setLayout(new GridBagLayout());
            GridBagConstraints c = gbc();
            c.gridwidth = 2;
            controls.add(sectionTitle("Connection"), c);

            c = gbc();
            c.gridy = 1;
            controls.add(label(I18n.get("tab.general.server_address.label")), c);
            serverAddress = new JComboBox<>(Proxy.getConfig().lastServersValue.reverseArray());
            serverAddress.setEditable(true);
            serverAddress.setSelectedItem(Proxy.getConfig().getServerAddress());
            c = gbc();
            c.gridy = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridwidth = 2;
            controls.add(serverAddress, c);

            c = gbc();
            c.gridy = 3;
            controls.add(label(I18n.get("tab.general.minecraft_account.label")), c);
            accounts = new JComboBox<>();
            accounts.setRenderer(new AccountRenderer());
            refreshAccounts();
            c = gbc();
            c.gridy = 4;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridwidth = 2;
            controls.add(accounts, c);

            statusLabel = value("Ready");
            endpointLabel = muted("127.0.0.1:25565");
            c = gbc();
            c.gridy = 5;
            c.gridwidth = 2;
            controls.add(statusLabel, c);
            c = gbc();
            c.gridy = 6;
            c.gridwidth = 2;
            controls.add(endpointLabel, c);

            JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
            actions.setOpaque(false);
            startButton = primaryButton(I18n.get("tab.general.state.start"));
            startButton.addActionListener(e -> {
                if (Proxy.isStarted()) stopProxy();
                else startProxy();
            });
            actions.add(startButton);
            pauseButton = softButton(I18n.get("tab.general.pause.suspend"));
            pauseButton.setEnabled(false);
            pauseButton.addActionListener(e -> {
                if (I18n.get("tab.general.pause.suspend").equalsIgnoreCase(pauseButton.getText())) {
                    Proxy.suspendRedirect();
                } else {
                    Proxy.resumeRedirect();
                }
            });
            actions.add(pauseButton);
            c = gbc();
            c.gridy = 7;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            controls.add(actions, c);
            c = gbc();
            c.gridy = 8;
            c.weighty = 1;
            controls.add(Box.createGlue(), c);

            leftClient = new ClientCard("Primary client");
            rightClient = new ClientCard("Clean client");
            JPanel clients = new JPanel(new GridLayout(1, 2, 14, 0));
            clients.setOpaque(false);
            clients.add(leftClient);
            clients.add(rightClient);

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.weightx = 0.42;
            gc.weighty = 1;
            gc.fill = GridBagConstraints.BOTH;
            gc.insets = new Insets(0, 0, 0, 8);
            grid.add(controls, gc);
            gc.gridx = 1;
            gc.weightx = 0.58;
            gc.insets = new Insets(0, 8, 0, 0);
            grid.add(clients, gc);

            Proxy.registerEvent(e -> SwingUtilities.invokeLater(() -> handleProxyEvent(e)));
        }

        private void refreshAccounts() {
            DefaultComboBoxModel<Account> model = new DefaultComboBoxModel<>();
            model.addElement(null);
            for (Account account : Proxy.getAccountManager().getAccounts()) model.addElement(account);
            accounts.setModel(model);
            if (Proxy.getConfig().account.get() != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    Account account = model.getElementAt(i);
                    if (account != null && Proxy.getConfig().account.get().equals(account.getName())) {
                        accounts.setSelectedItem(account);
                        break;
                    }
                }
            }
        }

        private void handleProxyEvent(net.java.faker.proxy.event.Event event) {
            if (event instanceof ProxyStateEvent stateEvent) {
                switch (stateEvent.getState()) {
                    case STARTING -> statusLabel.setText("Starting");
                    case STARTED -> {
                        statusLabel.setText("Running");
                        statusLabel.setForeground(GOOD);
                        endpointLabel.setText(Proxy.proxyAddress.getHostName() + ":" + Proxy.proxyAddress.getPort());
                    }
                    case STOPPING -> statusLabel.setText("Stopping");
                    case STOPPED -> {
                        statusLabel.setText("Ready");
                        statusLabel.setForeground(TEXT);
                    }
                }
            } else if (event instanceof RedirectStateChangeEvent changeEvent) {
                if (changeEvent.getState() == RedirectStateChangeEvent.State.PAUSED) {
                    pauseButton.setText(I18n.get("tab.general.pause.resume"));
                } else {
                    pauseButton.setText(I18n.get("tab.general.pause.suspend"));
                }
            } else if (event instanceof LoginEvent login) {
                if (leftConnection == null) {
                    leftConnection = login.getConnection();
                    leftClient.setConnection(leftConnection, true);
                } else if (rightConnection == null) {
                    rightConnection = login.getConnection();
                    rightClient.setConnection(rightConnection, false);
                }
            } else if (event instanceof DisconnectEvent disconnect) {
                if (disconnect.getConnection() == leftConnection) {
                    leftConnection = null;
                    leftClient.clear();
                } else if (disconnect.getConnection() == rightConnection) {
                    rightConnection = null;
                    rightClient.clear();
                }
            } else if (event instanceof SwapEvent swapEvent) {
                leftClient.setActive(swapEvent.getNewController() == leftConnection);
                rightClient.setActive(swapEvent.getNewController() == rightConnection);
            }
        }

        private void startProxy() {
            setComponentsEnabled(false);
            startButton.setEnabled(false);
            startButton.setText(I18n.get("tab.general.state.starting"));
            new Thread(() -> {
                String selectedServer = getSelectedServerAddress();
                try {
                    Config config = Proxy.getConfig();
                    config.setServerAddress(selectedServer);
                    config.lastServersValue.removeIgnoreCase(selectedServer);
                    config.lastServersValue.add(selectedServer);
                    while (config.lastServersValue.size() > 6) config.lastServersValue.remove(0);

                    if (accounts.getSelectedItem() instanceof Account account) {
                        Proxy.setAccount(account);
                        if (account.refresh()) Proxy.getAccountManager().save();
                    } else {
                        Proxy.setAccount(null);
                    }
                    advancedPage.prepareProxyStart();
                    applyGuiState();
                    Proxy.getConfig().save();
                    Proxy.startProxy();
                    SwingUtilities.invokeLater(() -> {
                        serverAddress.setModel(new DefaultComboBoxModel<>(Proxy.getConfig().lastServersValue.reverseArray()));
                        serverAddress.setSelectedItem(selectedServer);
                        startButton.setText(I18n.get("tab.general.state.stop"));
                        startButton.setEnabled(true);
                        pauseButton.setEnabled(WinRedirect.isSupported());
                    });
                } catch (Throwable e) {
                    Logger.error("Error while starting Proxy", e);
                    SwingUtilities.invokeLater(() -> {
                        showError(I18n.get("tab.general.error.failed_to_start", e.getMessage()));
                        setComponentsEnabled(true);
                        startButton.setText(I18n.get("tab.general.state.start"));
                        startButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    });
                }
            }, "Amethyst Start Proxy").start();
        }

        private void stopProxy() {
            startButton.setEnabled(false);
            pauseButton.setEnabled(false);
            Proxy.stopProxy();
            Timer timer = new Timer(800, e -> {
                setComponentsEnabled(true);
                startButton.setText(I18n.get("tab.general.state.start"));
                startButton.setEnabled(true);
                pauseButton.setText(I18n.get("tab.general.pause.suspend"));
                leftConnection = null;
                rightConnection = null;
                leftClient.clear();
                rightClient.clear();
            });
            timer.setRepeats(false);
            timer.start();
        }

        private void setComponentsEnabled(boolean enabled) {
            serverAddress.setEnabled(enabled);
            accounts.setEnabled(enabled);
            advancedPage.setRuntimeControlsEnabled(enabled);
        }

        private String getSelectedServerAddress() {
            return serverAddress.getSelectedItem() == null ? "" : serverAddress.getSelectedItem().toString().trim();
        }

        private void applyGuiState() {
            Proxy.getConfig().setServerAddress(getSelectedServerAddress());
            if (accounts.getSelectedItem() instanceof Account account) {
                Proxy.getConfig().account.set(account.getName());
            } else {
                Proxy.getConfig().account.set(null);
            }
        }
    }

    private class AdvancedPage extends Page {
        private final JCheckBox onlineMode;
        private final JCheckBox chatSigning;
        private final JCheckBox tracerouteFix;
        private final JCheckBox mdnsDisable;
        private final JCheckBox routerSpoof;
        private final JCheckBox blockTraffic;
        private final JCheckBox showKickErrors;
        private final JCheckBox allowDirectConnection;
        private final JCheckBox autoLatency;
        private final JCheckBox newPingCorrection;
        private final JCheckBox logPackets;
        private final JTextField proxy;
        private final NetworkAdapterComboBox networkAdapters;

        AdvancedPage() {
            super("Advanced", "Original options, same defaults, softer controls.");
            GlassPanel body = card();
            body.setLayout(new GridBagLayout());
            add(body, BorderLayout.NORTH);
            JPanel options = new JPanel(new GridLayout(0, 2, 12, 12));
            options.setOpaque(false);

            onlineMode = option(I18n.get("tab.advanced.proxy_online_mode.label"), Proxy.getConfig().onlineMode.get());
            chatSigning = option(I18n.get("tab.advanced.chat_signing.label"), Proxy.getConfig().signChat.get());
            options.add(onlineMode);
            options.add(chatSigning);

            if (WinRedirect.isSupported()) {
                tracerouteFix = option(I18n.get("tab.advanced.traceroute_fix.label"), Proxy.getConfig().tracerouteFix.get());
                mdnsDisable = option(I18n.get("tab.advanced.mdns_disable.label"), Proxy.getConfig().mdnsDisable.get());
                routerSpoof = option(I18n.get("tab.advanced.router_spoof.label"), Proxy.getConfig().routerSpoof.get());
                blockTraffic = option(I18n.get("tab.advanced.block_traffic.label"), Proxy.getConfig().blockTraffic.get());
                allowDirectConnection = option(I18n.get("tab.advanced.allow_direct_connection.label"), Proxy.getConfig().allowDirectConnection.get());
                autoLatency = option(I18n.get("tab.advanced.auto_latency.label"), Proxy.getConfig().autoLatency.get());
                newPingCorrection = option(I18n.get("tab.advanced.new_ping_correction.label"), Proxy.getConfig().newPingCorrection.get());
                logPackets = option(I18n.get("tab.advanced.log_packets.label"), Proxy.getConfig().logPackets.get());
                options.add(tracerouteFix);
                options.add(mdnsDisable);
                options.add(routerSpoof);
                options.add(blockTraffic);
                options.add(allowDirectConnection);
                options.add(autoLatency);
                options.add(newPingCorrection);
                options.add(logPackets);

                allowDirectConnection.addActionListener(e -> {
                    Proxy.proxyAddress = allowDirectConnection.isSelected()
                            ? new InetSocketAddress("0.0.0.0", 25565)
                            : new InetSocketAddress("127.0.0.1", 25565);
                });
                mdnsDisable.addActionListener(e -> new Thread(() -> {
                    SwingUtilities.invokeLater(() -> mdnsDisable.setEnabled(false));
                    if (mdnsDisable.isSelected()) Proxy.mdnsDisable();
                    else Proxy.mdnsRestore();
                    Util.sleep(500);
                    SwingUtilities.invokeLater(() -> mdnsDisable.setEnabled(true));
                }, "Amethyst mDNS").start());
            } else {
                tracerouteFix = null;
                mdnsDisable = null;
                routerSpoof = null;
                blockTraffic = null;
                allowDirectConnection = option(I18n.get("tab.advanced.allow_direct_connection.label"), Proxy.getConfig().allowDirectConnection.get());
                autoLatency = option(I18n.get("tab.advanced.auto_latency.label"), Proxy.getConfig().autoLatency.get());
                newPingCorrection = option(I18n.get("tab.advanced.new_ping_correction.label"), Proxy.getConfig().newPingCorrection.get());
                logPackets = option(I18n.get("tab.advanced.log_packets.label"), Proxy.getConfig().logPackets.get());
                options.add(allowDirectConnection);
                options.add(autoLatency);
                options.add(newPingCorrection);
                options.add(logPackets);
            }
            showKickErrors = option(I18n.get("tab.advanced.show_kick_errors.label"), Proxy.getConfig().showKickErrors.get());
            options.add(showKickErrors);

            GridBagConstraints c = gbc();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(options, c);

            JPanel adapterPanel = new JPanel(new GridBagLayout());
            adapterPanel.setOpaque(false);
            JLabel adapterLabel = label(I18n.get("tab.advanced.network_interface.label"));
            networkAdapters = new NetworkAdapterComboBox(ni -> {
                if (ni.hasInternetAccess()) showWarning(String.format(I18n.get("tab.advanced.error.internet_adapter"), ni));
            });
            networkAdapters.setValueCanBeNull(true);
            networkAdapters.fillAdapters(interfaces -> {
                if (Proxy.getConfig().targetAdapter.get() == null) {
                    NetworkInterface potentialInterface = NetworkUtil.findPotentialWifiHotspotInterface(interfaces);
                    if (potentialInterface != null) networkAdapters.setSelectedItem(potentialInterface);
                } else if ("null".equals(Proxy.getConfig().targetAdapter.get())) {
                    networkAdapters.setSelectedItem(0);
                } else {
                    String targetAdapter = Proxy.getConfig().targetAdapter.get();
                    for (NetworkInterface ni : interfaces) {
                        if (targetAdapter.equals(NetworkUtil.toWindowsMac(ni.getHardwareAddress()))) {
                            networkAdapters.setSelectedItem(ni);
                            break;
                        }
                    }
                }
            });
            if (routerSpoof != null) {
                routerSpoof.addActionListener(e -> updateNetworkAdapterEnabled());
                blockTraffic.addActionListener(e -> updateNetworkAdapterEnabled());
            }
            c = gbc();
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            adapterPanel.add(adapterLabel, c);
            c = gbc();
            c.gridy = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            adapterPanel.add(networkAdapters, c);

            proxy = new JTextField();
            proxy.setText(Proxy.getConfig().proxy.get());
            c = gbc();
            c.gridy = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(adapterPanel, c);
            c = gbc();
            c.gridy = 3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(label(I18n.get("tab.advanced.proxy_url.label")), c);
            c = gbc();
            c.gridy = 4;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(proxy, c);
            updateNetworkAdapterEnabled();
        }

        private void updateNetworkAdapterEnabled() {
            if (routerSpoof == null || blockTraffic == null) {
                networkAdapters.setEnabled(false);
                return;
            }
            networkAdapters.setEnabled(routerSpoof.isSelected() || blockTraffic.isSelected());
        }

        private void prepareProxyStart() throws Exception {
            if (networkAdapters.getSelectedItem() instanceof NetworkInterface ni) Proxy.setTargetAdapter(ni);
            String proxyUrl = proxy.getText() == null ? "" : proxy.getText().trim();
            Proxy.setBackendProxy(proxyUrl.isBlank() ? null : new URI(proxyUrl));
        }

        private void setRuntimeControlsEnabled(boolean enabled) {
            onlineMode.setEnabled(enabled);
            chatSigning.setEnabled(enabled);
            showKickErrors.setEnabled(enabled);
            allowDirectConnection.setEnabled(enabled);
            autoLatency.setEnabled(enabled);
            newPingCorrection.setEnabled(enabled);
            logPackets.setEnabled(enabled);
            proxy.setEnabled(enabled);
            if (tracerouteFix != null) {
                tracerouteFix.setEnabled(enabled);
                routerSpoof.setEnabled(enabled);
                blockTraffic.setEnabled(enabled);
                updateNetworkAdapterEnabled();
            }
        }

        private void applyGuiState() {
            Config config = Proxy.getConfig();
            config.onlineMode.set(onlineMode.isSelected());
            config.signChat.set(chatSigning.isSelected());
            config.showKickErrors.set(showKickErrors.isSelected());
            config.allowDirectConnection.set(allowDirectConnection.isSelected());
            config.autoLatency.set(autoLatency.isSelected());
            config.newPingCorrection.set(newPingCorrection.isSelected());
            config.logPackets.set(logPackets.isSelected());
            config.proxy.set(proxy.getText());
            if (tracerouteFix != null) {
                config.tracerouteFix.set(tracerouteFix.isSelected());
                config.mdnsDisable.set(mdnsDisable.isSelected());
                config.blockTraffic.set(blockTraffic.isSelected());
                config.routerSpoof.set(routerSpoof.isSelected());
                if (networkAdapters.getSelectedItem() == NetworkInterface.NULL) {
                    config.targetAdapter.set("null");
                } else if (networkAdapters.getSelectedItem() instanceof NetworkInterface ni) {
                    config.targetAdapter.set(NetworkUtil.toWindowsMac(ni.getHardwareAddress()));
                }
            }
        }
    }

    private class AccountsPage extends Page {
        private final DefaultListModel<Account> model = new DefaultListModel<>();
        private final JList<Account> list = new JList<>(model);
        private final JButton addMicrosoft;
        private AddAccountPopup popup;
        private Thread addThread;

        AccountsPage() {
            super("Accounts", "Microsoft accounts are stored by the original account manager.");
            GlassPanel body = card();
            body.setLayout(new BorderLayout(0, 12));
            add(body, BorderLayout.CENTER);

            list.setCellRenderer(new AccountRenderer());
            list.setOpaque(false);
            list.setFixedCellHeight(36);
            refresh();
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setOpaque(false);
            scroll.setOpaque(false);
            body.add(scroll, BorderLayout.CENTER);

            JPopupMenu menu = new JPopupMenu();
            JMenuItem remove = new JMenuItem(I18n.get("tab.accounts.list.context_menu.remove"));
            remove.addActionListener(e -> removeSelected());
            JMenuItem up = new JMenuItem(I18n.get("tab.accounts.list.context_menu.move_up"));
            up.addActionListener(e -> moveSelected(-1));
            JMenuItem down = new JMenuItem(I18n.get("tab.accounts.list.context_menu.move_down"));
            down.addActionListener(e -> moveSelected(1));
            menu.add(remove);
            menu.add(up);
            menu.add(down);
            list.setComponentPopupMenu(menu);

            addMicrosoft = primaryButton(I18n.get("tab.accounts.add_microsoft.label"));
            addMicrosoft.addActionListener(e -> {
                addMicrosoft.setEnabled(false);
                handleLogin(msaDeviceCodeConsumer -> new MicrosoftAccount(MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
                        MinecraftAuth.createHttpClient(),
                        new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCodeConsumer)
                )));
            });
            body.add(addMicrosoft, BorderLayout.SOUTH);
        }

        private void refresh() {
            model.removeAllElements();
            Proxy.getAccountManager().getAccounts().forEach(model::addElement);
            if (dashboardPage != null) dashboardPage.refreshAccounts();
        }

        private void removeSelected() {
            int index = list.getSelectedIndex();
            if (index < 0) return;
            Account account = model.remove(index);
            Proxy.getAccountManager().removeAccount(account);
            Proxy.getAccountManager().save();
            refresh();
            if (index < model.size()) list.setSelectedIndex(index);
            else if (index > 0) list.setSelectedIndex(index - 1);
        }

        private void moveSelected(int delta) {
            int index = list.getSelectedIndex();
            int newIndex = index + delta;
            if (index < 0 || newIndex < 0 || newIndex >= model.size()) return;
            Account account = model.remove(index);
            model.add(newIndex, account);
            list.setSelectedIndex(newIndex);
            Proxy.getAccountManager().removeAccount(account);
            Proxy.getAccountManager().addAccount(newIndex, account);
            Proxy.getAccountManager().save();
            refresh();
        }

        private void closePopup() {
            if (popup != null) {
                popup.markExternalClose();
                popup.setVisible(false);
                popup.dispose();
                popup = null;
            }
            addMicrosoft.setEnabled(true);
        }

        private void handleLogin(TFunction<Consumer<StepMsaDeviceCode.MsaDeviceCode>, Account> requestHandler) {
            addThread = new Thread(() -> {
                try {
                    Account account = requestHandler.apply(msaDeviceCode -> SwingUtilities.invokeLater(() -> new AddAccountPopup(
                            AmethystWindow.this,
                            msaDeviceCode,
                            popup -> this.popup = popup,
                            () -> {
                                closePopup();
                                addThread.interrupt();
                            }
                    )));
                    SwingUtilities.invokeLater(() -> {
                        closePopup();
                        Proxy.getAccountManager().addAccount(account);
                        Proxy.getAccountManager().save();
                        refresh();
                        showInfo(I18n.get("tab.accounts.add.success", account.getName()));
                    });
                } catch (InterruptedException ignored) {
                } catch (TimeoutException e) {
                    SwingUtilities.invokeLater(() -> {
                        closePopup();
                        showError(I18n.get("tab.accounts.add.timeout", "60"));
                    });
                } catch (Throwable t) {
                    SwingUtilities.invokeLater(() -> {
                        closePopup();
                        showException(t);
                    });
                }
            }, "Amethyst Add Account");
            addThread.setDaemon(true);
            addThread.start();
        }
    }

    private class DhcpPage extends Page {
        private final NetworkAdapterComboBox networkAdapters;
        private final JTextField ipAddress = new JTextField();
        private final JTextField mask = new JTextField();
        private final JTextField startIp = new JTextField();
        private final JTextField endIp = new JTextField();
        private final JTextField dns1 = new JTextField();
        private final JTextField dns2 = new JTextField();
        private final JButton start = primaryButton(I18n.get("tab.general.state.start"));
        private String lastGeneratedIp;
        private final Random random = new Random();

        DhcpPage() {
            super("DHCP", "The original DHCP helper in a denser layout.");
            GlassPanel body = card();
            body.setLayout(new GridBagLayout());
            add(body, BorderLayout.NORTH);

            networkAdapters = new NetworkAdapterComboBox(ni -> {
                if (ni.hasInternetAccess()) showWarning(String.format(I18n.get("tab.dhcp.error.internet_adapter"), ni));
            });
            networkAdapters.fillAdapters(interfaces -> {
                if (Proxy.getConfig().dhcp_interface.get() == null) {
                    NetworkInterface potentialInterface = NetworkUtil.findPotentialWifiHotspotInterface(interfaces);
                    if (potentialInterface != null) networkAdapters.setSelectedItem(potentialInterface);
                } else {
                    String targetAdapter = Proxy.getConfig().dhcp_interface.get();
                    for (NetworkInterface ni : interfaces) {
                        if (targetAdapter.equals(NetworkUtil.toWindowsMac(ni.getHardwareAddress()))) {
                            networkAdapters.setSelectedItem(ni);
                            break;
                        }
                    }
                }
                if (Proxy.getConfig().dhcp_started.get() && networkAdapters.getSelectedItem() instanceof NetworkInterface ni) {
                    start(ni, Proxy.getConfig().dhcp_ip.get(), Proxy.getConfig().dhcp_mask.get(), Proxy.getConfig().dhcp_startIp.get(), Proxy.getConfig().dhcp_endIp.get(), new String[]{Proxy.getConfig().dhcp_dns1.get(), Proxy.getConfig().dhcp_dns2.get()});
                }
            });

            ipAddress.setText(Proxy.getConfig().dhcp_ip.get());
            lastGeneratedIp = Proxy.getConfig().dhcp_ip.get();
            mask.setText(Proxy.getConfig().dhcp_mask.get());
            startIp.setText(Proxy.getConfig().dhcp_startIp.get());
            endIp.setText(Proxy.getConfig().dhcp_endIp.get());
            dns1.setText(Proxy.getConfig().dhcp_dns1.get());
            dns2.setText(Proxy.getConfig().dhcp_dns2.get());
            ipAddress.getDocument().addDocumentListener(new SimpleDocumentListener(() -> start.setText(ipAddress.getText().isEmpty() ? I18n.get("tab.dhcp.generate") : I18n.get("tab.general.state.start"))));

            JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
            grid.setOpaque(false);
            grid.add(field("Network interface", networkAdapters));
            grid.add(field("IP address", ipAddress));
            grid.add(field("Mask", mask));
            grid.add(field("Start ip", startIp));
            grid.add(field("End ip", endIp));
            grid.add(field("DNS 1", dns1));
            grid.add(field("DNS 2", dns2));
            GridBagConstraints c = gbc();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(grid, c);

            start.addActionListener(e -> handleStartStop());
            c = gbc();
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(start, c);
        }

        private void handleStartStop() {
            try {
                if (I18n.get("tab.general.state.stop").equals(start.getText())) {
                    Proxy.getConfig().dhcp_started.set(false);
                    Proxy.getConfig().save();
                    stop();
                    return;
                }
                if (!(networkAdapters.getSelectedItem() instanceof NetworkInterface ni)) {
                    showError("Network interface is not selected");
                    return;
                }
                if (ni.hasInternetAccess()) {
                    showError("Network interface " + ni.getDisplayName() + " has internet access. Please choose another");
                    return;
                }
                String ip = ipAddress.getText();
                String startValue = startIp.getText();
                String endValue = endIp.getText();
                String dns1Value = dns1.getText();
                String dns2Value = dns2.getText();
                boolean shouldStart = true;
                if (ip.isEmpty()) {
                    ip = generateIp();
                    shouldStart = false;
                }
                if (shouldStart && !NetworkUtil.isIpv4(ip)) {
                    showError("'" + ip + "' is not valid ip address");
                    return;
                }
                if (ip.startsWith("192.168.137")) {
                    showError("'" + ip + "' cannot be used");
                    return;
                }
                Inet4Address address = (Inet4Address) InetAddress.getByName(ip);
                String maskValue = mask.getText();
                if (maskValue.isEmpty()) {
                    maskValue = "255.255.255.0";
                    mask.setText(maskValue);
                } else if (NetworkUtil.getPrefix(maskValue) == -1) {
                    showError("'" + maskValue + "' is not valid mask");
                    return;
                }
                int prefix = NetworkUtil.getPrefix(maskValue);
                if (startValue.isEmpty() || !NetworkUtil.isIpInSameNetwork(prefix, ip, startValue)) {
                    startValue = NetworkUtil.fromIntAddress(NetworkUtil.getStartInt(address, prefix) + 1).getHostAddress();
                }
                if (endValue.isEmpty() || !NetworkUtil.isIpInSameNetwork(prefix, ip, endValue)) {
                    endValue = NetworkUtil.fromIntAddress(NetworkUtil.getEndInt(address, prefix)).getHostAddress();
                }
                if (dns1Value.isEmpty() || dns1Value.equals(lastGeneratedIp)) dns1Value = ip;
                if (!dns2Value.isEmpty() && !NetworkUtil.isIpv4(dns2Value)) dns2Value = "";

                lastGeneratedIp = ip;
                ipAddress.setText(ip);
                mask.setText(maskValue);
                startIp.setText(startValue);
                endIp.setText(endValue);
                dns1.setText(dns1Value);
                dns2.setText(dns2Value);
                applyGuiState();
                if (shouldStart) {
                    Proxy.getConfig().dhcp_started.set(true);
                    start(ni, ip, maskValue, startValue, endValue, new String[]{dns1Value, dns2Value});
                }
                Proxy.getConfig().save();
            } catch (Exception ex) {
                Logger.error("Failed to parse dhcp", ex);
                showError(ex.getMessage());
            }
        }

        private void start(NetworkInterface ni, String ip, String mask, String startIp, String endIp, String[] dns) {
            start.setText(I18n.get("tab.general.state.starting"));
            start.setEnabled(false);
            setState(false);
            new Thread(() -> {
                try {
                    Dhcp.start(ni, ip, mask, startIp, endIp, dns);
                    Logger.info("Dhcp started successfully");
                    SwingUtilities.invokeLater(() -> {
                        start.setEnabled(true);
                        start.setText(I18n.get("tab.general.state.stop"));
                    });
                    networkAdapters.fillAdapters(null);
                } catch (Throwable e) {
                    Logger.error("Failed to start dhcp", e);
                    SwingUtilities.invokeLater(() -> {
                        start.setEnabled(true);
                        setState(true);
                        start.setText(I18n.get("tab.general.state.start"));
                        showError("Failed to start dhcp: " + e.getMessage());
                    });
                }
            }, "Amethyst DHCP Start").start();
        }

        private void stop() {
            start.setEnabled(false);
            new Thread(() -> {
                try {
                    Dhcp.stop();
                    Logger.info("Dhcp stopped successfully");
                    SwingUtilities.invokeLater(() -> {
                        start.setEnabled(true);
                        setState(true);
                        start.setText(I18n.get("tab.general.state.start"));
                    });
                } catch (Throwable e) {
                    Logger.error("Failed to stop dhcp", e);
                    SwingUtilities.invokeLater(() -> {
                        start.setEnabled(true);
                        start.setText(I18n.get("tab.general.state.stop"));
                        showError("Failed to stop dhcp: " + e.getMessage());
                    });
                }
            }, "Amethyst DHCP Stop").start();
        }

        private String generateIp() {
            for (int i = 0; i < 100; i++) {
                String ip = "192.168." + random.nextInt(255) + ".1";
                if (!ip.equals("192.168.137.1") && !NetworkUtil.localIpExists(ip, null)) return ip;
            }
            return "";
        }

        private void setState(boolean enabled) {
            networkAdapters.setEnabled(enabled);
            ipAddress.setEnabled(enabled);
            mask.setEnabled(enabled);
            startIp.setEnabled(enabled);
            endIp.setEnabled(enabled);
            dns1.setEnabled(enabled);
            dns2.setEnabled(enabled);
        }

        private void applyGuiState() {
            Config config = Proxy.getConfig();
            config.dhcp_ip.set(ipAddress.getText());
            config.dhcp_mask.set(mask.getText());
            config.dhcp_startIp.set(startIp.getText());
            config.dhcp_endIp.set(endIp.getText());
            config.dhcp_dns1.set(dns1.getText());
            config.dhcp_dns2.set(dns2.getText());
            if (networkAdapters.getSelectedItem() instanceof NetworkInterface ni) {
                config.dhcp_interface.set(NetworkUtil.toWindowsMac(ni.getHardwareAddress()));
            } else {
                config.dhcp_interface.set(null);
            }
        }
    }

    private class SettingsPage extends Page {
        SettingsPage() {
            super("Settings", "Language and shell selection.");
            GlassPanel body = card();
            body.setLayout(new GridBagLayout());
            add(body, BorderLayout.NORTH);

            JComboBox<String> language = new JComboBox<>(I18n.getAvailableLocales().toArray(new String[0]));
            language.setSelectedItem(I18n.getCurrentLocale());
            language.addActionListener(e -> {
                if (language.getSelectedItem() instanceof String locale && !locale.equals(I18n.getCurrentLocale())) {
                    I18n.setLocale(locale);
                    I18n.update();
                }
            });
            JComboBox<String> shell = new JComboBox<>(new String[]{"Faker Custom UI fork by anydefax", "Classic"});
            shell.setSelectedIndex("classic".equalsIgnoreCase(Proxy.getConfig().uiShell.get()) ? 1 : 0);
            shell.addActionListener(e -> {
                if (shell.getSelectedIndex() == 1) switchToClassic();
                else {
                    Proxy.getConfig().uiShell.set("custom");
                    Proxy.getConfig().save();
                    setTitle(APP_NAME + " " + Proxy.VERSION);
                }
            });
            JComboBox<Theme> themeSelector = new JComboBox<>(Theme.values());
            themeSelector.setSelectedItem(theme);
            themeSelector.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Theme option) label.setText(option.displayName);
                    return label;
                }
            });
            JPanel customTheme = new JPanel(new GridLayout(0, 2, 12, 12));
            customTheme.setOpaque(false);
            JTextField customBackground = colorField(Proxy.getConfig().uiCustomBackground.get());
            JTextField customSurface = colorField(Proxy.getConfig().uiCustomSurface.get());
            JTextField customAccent = colorField(Proxy.getConfig().uiCustomAccent.get());
            JTextField customAccentSoft = colorField(Proxy.getConfig().uiCustomAccentSoft.get());
            JTextField customText = colorField(Proxy.getConfig().uiCustomText.get());
            JTextField customMuted = colorField(Proxy.getConfig().uiCustomMuted.get());
            customTheme.add(field("Background", customBackground));
            customTheme.add(field("Glass surface", customSurface));
            customTheme.add(field("Accent", customAccent));
            customTheme.add(field("Accent soft", customAccentSoft));
            customTheme.add(field("Text", customText));
            customTheme.add(field("Muted text", customMuted));
            JButton applyCustomTheme = softButton("Apply custom theme");
            applyCustomTheme.addActionListener(e -> {
                if (!saveCustomTheme(customBackground, customSurface, customAccent, customAccentSoft, customText, customMuted)) return;
                themeSelector.setSelectedItem(Theme.CUSTOM);
                Proxy.getConfig().uiTheme.set(Theme.CUSTOM.id);
                Proxy.getConfig().save();
                setTheme(Theme.CUSTOM, true);
            });
            themeSelector.addActionListener(e -> {
                if (!(themeSelector.getSelectedItem() instanceof Theme selected) || selected == theme) return;
                Proxy.getConfig().uiTheme.set(selected.id);
                Proxy.getConfig().save();
                setTheme(selected, true);
                customTheme.setVisible(selected == Theme.CUSTOM);
                applyCustomTheme.setVisible(selected == Theme.CUSTOM);
                body.revalidate();
                body.repaint();
            });
            customTheme.setVisible(theme == Theme.CUSTOM);
            applyCustomTheme.setVisible(theme == Theme.CUSTOM);

            GridBagConstraints c = gbc();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(field("Language", language), c);
            c = gbc();
            c.gridy = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(field("UI shell / app", shell), c);
            c = gbc();
            c.gridy = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(field("UI theme", themeSelector), c);
            c = gbc();
            c.gridy = 3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(customTheme, c);
            c = gbc();
            c.gridy = 4;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            body.add(applyCustomTheme, c);
        }

        private JTextField colorField(String value) {
            JTextField field = new JTextField(normalizeHex(value, "#000000"));
            field.setColumns(8);
            return field;
        }

        private boolean saveCustomTheme(JTextField background, JTextField surface, JTextField accent,
                                        JTextField accentSoft, JTextField text, JTextField muted) {
            try {
                Proxy.getConfig().uiCustomBackground.set(normalizeHexOrThrow(background.getText()));
                Proxy.getConfig().uiCustomSurface.set(normalizeHexOrThrow(surface.getText()));
                Proxy.getConfig().uiCustomAccent.set(normalizeHexOrThrow(accent.getText()));
                Proxy.getConfig().uiCustomAccentSoft.set(normalizeHexOrThrow(accentSoft.getText()));
                Proxy.getConfig().uiCustomText.set(normalizeHexOrThrow(text.getText()));
                Proxy.getConfig().uiCustomMuted.set(normalizeHexOrThrow(muted.getText()));
                background.setText(Proxy.getConfig().uiCustomBackground.get());
                surface.setText(Proxy.getConfig().uiCustomSurface.get());
                accent.setText(Proxy.getConfig().uiCustomAccent.get());
                accentSoft.setText(Proxy.getConfig().uiCustomAccentSoft.get());
                text.setText(Proxy.getConfig().uiCustomText.get());
                muted.setText(Proxy.getConfig().uiCustomMuted.get());
                return true;
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return false;
            }
        }
    }

    private enum Theme {
        CUSTOM(
                "custom", "Custom UI",
                new Color(10, 9, 6), new Color(22, 19, 10), new Color(3, 3, 3),
                new Color(34, 29, 17, 220), new Color(58, 47, 20, 238), new Color(244, 198, 68, 108),
                new Color(235, 181, 45), new Color(255, 225, 126),
                new Color(255, 247, 222), new Color(206, 185, 134), new Color(10, 8, 4),
                new Color(49, 42, 24), new Color(31, 27, 18),
                new Color(114, 218, 147), new Color(255, 195, 72),
                new Color(193, 135, 22), new Color(255, 215, 95), new Color(255, 218, 113, 10)
        ),
        AMETHYST(
                "amethyst", "Amethyst UI",
                new Color(11, 12, 18), new Color(20, 18, 30), new Color(8, 13, 18),
                new Color(29, 28, 39, 214), new Color(47, 43, 62, 232), new Color(189, 158, 231, 88),
                new Color(167, 126, 214), new Color(206, 181, 232),
                new Color(239, 236, 244), new Color(178, 171, 188), Color.WHITE,
                new Color(41, 39, 51), new Color(28, 27, 37),
                new Color(107, 217, 162), new Color(239, 189, 114),
                new Color(113, 82, 150), new Color(96, 189, 178), new Color(255, 255, 255, 8)
        ),
        PLATINUM(
                "platinum", "Platinum UI",
                new Color(12, 14, 17), new Color(24, 28, 33), new Color(8, 10, 12),
                new Color(34, 38, 44, 218), new Color(56, 63, 72, 236), new Color(210, 220, 230, 96),
                new Color(176, 193, 210), new Color(234, 240, 246),
                new Color(245, 248, 250), new Color(179, 190, 201), new Color(10, 12, 15),
                new Color(43, 49, 56), new Color(30, 35, 40),
                new Color(108, 221, 181), new Color(238, 194, 111),
                new Color(160, 180, 202), new Color(91, 164, 190), new Color(255, 255, 255, 10)
        ),
        CRIMSON(
                "crimson", "Crimson UI",
                new Color(18, 9, 12), new Color(35, 14, 22), new Color(11, 11, 15),
                new Color(43, 26, 35, 220), new Color(70, 31, 45, 238), new Color(235, 86, 120, 100),
                new Color(224, 68, 99), new Color(255, 168, 188),
                new Color(255, 239, 243), new Color(206, 164, 174), Color.WHITE,
                new Color(54, 35, 44), new Color(34, 24, 30),
                new Color(105, 222, 164), new Color(245, 185, 92),
                new Color(168, 24, 54), new Color(224, 76, 106), new Color(255, 210, 220, 8)
        ),
        WHITE_BLACK(
                "white-black", "White-black UI",
                new Color(230, 232, 235), new Color(248, 249, 250), new Color(28, 30, 34),
                new Color(255, 255, 255, 222), new Color(240, 242, 245, 238), new Color(28, 30, 34, 76),
                new Color(22, 24, 28), new Color(70, 73, 79),
                new Color(18, 20, 23), new Color(91, 97, 106), Color.WHITE,
                new Color(248, 249, 250), new Color(242, 244, 247),
                new Color(29, 150, 99), new Color(180, 118, 28),
                new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 0, 0, 7)
        ),
        PREMIUM_PRESTIGE(
                "premium-prestige", "Premium Prestige UI",
                new Color(10, 9, 6), new Color(22, 19, 10), new Color(3, 3, 3),
                new Color(34, 29, 17, 220), new Color(58, 47, 20, 238), new Color(244, 198, 68, 108),
                new Color(235, 181, 45), new Color(255, 225, 126),
                new Color(255, 247, 222), new Color(206, 185, 134), new Color(10, 8, 4),
                new Color(49, 42, 24), new Color(31, 27, 18),
                new Color(114, 218, 147), new Color(255, 195, 72),
                new Color(193, 135, 22), new Color(255, 215, 95), new Color(255, 218, 113, 10)
        );

        final String id;
        final String displayName;
        final Color background;
        final Color gradientStart;
        final Color gradientEnd;
        final Color surface;
        final Color surfaceHover;
        final Color stroke;
        final Color accent;
        final Color accentSoft;
        final Color text;
        final Color muted;
        final Color selectionText;
        final Color control;
        final Color list;
        final Color good;
        final Color warn;
        final Color glowPrimary;
        final Color glowSecondary;
        final Color glassLine;

        Theme(String id, String displayName, Color background, Color gradientStart, Color gradientEnd,
              Color surface, Color surfaceHover, Color stroke, Color accent, Color accentSoft,
              Color text, Color muted, Color selectionText, Color control, Color list,
              Color good, Color warn, Color glowPrimary, Color glowSecondary, Color glassLine) {
            this.id = id;
            this.displayName = displayName;
            this.background = background;
            this.gradientStart = gradientStart;
            this.gradientEnd = gradientEnd;
            this.surface = surface;
            this.surfaceHover = surfaceHover;
            this.stroke = stroke;
            this.accent = accent;
            this.accentSoft = accentSoft;
            this.text = text;
            this.muted = muted;
            this.selectionText = selectionText;
            this.control = control;
            this.list = list;
            this.good = good;
            this.warn = warn;
            this.glowPrimary = glowPrimary;
            this.glowSecondary = glowSecondary;
            this.glassLine = glassLine;
        }

        static Theme fromId(String id) {
            for (Theme theme : values()) {
                if (theme.id.equalsIgnoreCase(String.valueOf(id))) return theme;
            }
            return PREMIUM_PRESTIGE;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private abstract class Page extends JPanel {
        private float pageAlpha = 1f;

        Page(String title, String subtitle) {
            setOpaque(false);
            setBorder(new EmptyBorder(4, 4, 4, 4));
            setLayout(new BorderLayout(16, 16));
            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            JLabel h = new JLabel(title);
            h.setForeground(TEXT);
            h.setFont(h.getFont().deriveFont(Font.BOLD, 30f));
            JLabel s = muted(subtitle);
            header.add(h);
            header.add(Box.createVerticalStrut(4));
            header.add(s);
            add(header, BorderLayout.NORTH);
        }

        void setPageAlpha(float pageAlpha) {
            this.pageAlpha = Math.max(0f, Math.min(1f, pageAlpha));
            repaint();
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setComposite(AlphaComposite.SrcOver.derive(pageAlpha));
            super.paint(g);
            g.dispose();
        }
    }

    private class AnimatedPages extends JPanel {
        private final Map<String, Component> pageMap = new LinkedHashMap<>();
        private Timer transitionTimer;
        private String currentId;

        AnimatedPages() {
            setLayout(null);
            setOpaque(false);
        }

        void addPage(String id, Component page) {
            pageMap.put(id, page);
            page.setVisible(false);
            add(page);
        }

        void showPage(String id) {
            Component target = pageMap.get(id);
            if (target == null || id.equals(currentId)) return;
            Component current = currentId == null ? null : pageMap.get(currentId);
            int direction = getPageIndex(id) >= getPageIndex(currentId) ? 1 : -1;
            Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());

            if (transitionTimer != null && transitionTimer.isRunning()) transitionTimer.stop();
            if (current == null || getWidth() <= 0) {
                currentId = id;
                target.setBounds(bounds);
                target.setVisible(true);
                setAlpha(target, 1f);
                revalidate();
                repaint();
                return;
            }

            long started = System.currentTimeMillis();
            int distance = Math.max(36, Math.min(96, getWidth() / 9));
            target.setBounds(direction * distance, 0, bounds.width, bounds.height);
            setAlpha(target, 0f);
            target.setVisible(true);
            setComponentZOrder(target, 0);
            setComponentZOrder(current, 1);

            transitionTimer = new Timer(16, e -> {
                float raw = Math.min(1f, (System.currentTimeMillis() - started) / 260f);
                float eased = easeOutCubic(raw);
                int currentX = Math.round(-direction * distance * eased);
                int targetX = Math.round(direction * distance * (1f - eased));
                current.setBounds(currentX, 0, bounds.width, bounds.height);
                target.setBounds(targetX, 0, bounds.width, bounds.height);
                setAlpha(current, 1f - eased * 0.82f);
                setAlpha(target, eased);
                if (raw >= 1f) {
                    ((Timer) e.getSource()).stop();
                    current.setVisible(false);
                    current.setBounds(bounds);
                    target.setBounds(bounds);
                    setAlpha(current, 1f);
                    setAlpha(target, 1f);
                    currentId = id;
                }
                repaint();
            });
            transitionTimer.start();
        }

        private int getPageIndex(String id) {
            if (id == null) return -1;
            int index = 0;
            for (String key : pageMap.keySet()) {
                if (key.equals(id)) return index;
                index++;
            }
            return -1;
        }

        @Override
        public void doLayout() {
            Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
            for (Component page : pageMap.values()) {
                if (transitionTimer == null || !transitionTimer.isRunning()) page.setBounds(bounds);
            }
        }
    }

    private class AmethystRootPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());
            float glow = (float) ((Math.sin(pulse) + 1) / 2);
            g.setPaint(new GradientPaint(0, 0, GRADIENT_START, getWidth(), getHeight(), GRADIENT_END));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(new RadialGradientPaint(
                    new Point((int) (getWidth() * 0.18), (int) (getHeight() * 0.16)),
                    Math.max(getWidth(), getHeight()) * 0.62f,
                    new float[]{0f, 1f},
                    new Color[]{withAlpha(GLOW_PRIMARY, 52 + (int) (glow * 24)), withAlpha(BACKGROUND, 0)}
            ));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setPaint(new RadialGradientPaint(
                    new Point((int) (getWidth() * 0.86), (int) (getHeight() * 0.82)),
                    Math.max(getWidth(), getHeight()) * 0.48f,
                    new float[]{0f, 1f},
                    new Color[]{withAlpha(GLOW_SECONDARY, 34), withAlpha(BACKGROUND, 0)}
            ));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(GLASS_LINE);
            for (int x = -80; x < getWidth(); x += 92) {
                g.drawLine(x, 0, x + getHeight(), getHeight());
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private class GlassPanel extends JPanel {
        private boolean highlighted;

        GlassPanel() {
            setOpaque(false);
        }

        void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 22;
            int glow = highlighted ? 44 : 18;
            Shape shape = new RoundRectangle2D.Float(1, 1, Math.max(0, getWidth() - 2), Math.max(0, getHeight() - 2), arc, arc);
            g.setColor(new Color(0, 0, 0, 72));
            g.fillRoundRect(4, 7, Math.max(0, getWidth() - 8), Math.max(0, getHeight() - 8), arc, arc);
            g.setPaint(new GradientPaint(0, 0, highlighted ? SURFACE_HOVER : SURFACE, 0, getHeight(), withAlpha(GRADIENT_END, 226)));
            g.fill(shape);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 34), getWidth(), getHeight(), withAlpha(AMETHYST, glow)));
            g.draw(shape);
            if (highlighted) {
                g.setPaint(new RadialGradientPaint(
                        new Point(getWidth() - 34, 32),
                        Math.max(80, getWidth() / 2),
                        new float[]{0f, 1f},
                        new Color[]{withAlpha(AMETHYST, 52), withAlpha(AMETHYST, 0)}
                ));
                g.fill(shape);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private class NavButton extends JButton {
        private boolean selected;
        private float selectedProgress;
        private float hoverProgress;
        private float pressProgress;
        private final Timer animator;

        NavButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorder(new EmptyBorder(13, 16, 13, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(TEXT);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(10, 46));
            getModel().addChangeListener(e -> {
                ensureAnimation();
                repaint();
            });
            animator = new Timer(16, e -> {
                boolean changed = false;
                changed |= approachSelected();
                changed |= approachHover();
                changed |= approachPress();
                if (!changed) ((Timer) e.getSource()).stop();
                repaint();
            });
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
            ensureAnimation();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            float fill = Math.min(1f, selectedProgress + hoverProgress * 0.42f);
            if (fill > 0.01f) {
                g.setColor(mix(withAlpha(CONTROL, 0), withAlpha(AMETHYST, 142), fill));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g.setColor(new Color(255, 255, 255, Math.round(22 * fill)));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            if (selectedProgress > 0.01f) {
                int indicatorHeight = Math.round((getHeight() - 16) * selectedProgress);
                g.setColor(AMETHYST_SOFT);
                g.fillRoundRect(0, (getHeight() - indicatorHeight) / 2, 4, indicatorHeight, 4, 4);
            }
            if (pressProgress > 0.01f) {
                g.setColor(new Color(255, 255, 255, Math.round(24 * pressProgress)));
                g.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 14, 14);
            }
            g.dispose();
            super.paintComponent(graphics);
        }

        private void ensureAnimation() {
            if (!animator.isRunning()) animator.start();
        }

        private boolean approachSelected() {
            return approachValue("selected");
        }

        private boolean approachHover() {
            return approachValue("hover");
        }

        private boolean approachPress() {
            return approachValue("press");
        }

        private boolean approachValue(String field) {
            float target;
            float value;
            if ("selected".equals(field)) {
                target = selected ? 1f : 0f;
                value = selectedProgress;
            } else if ("hover".equals(field)) {
                target = getModel().isRollover() ? 1f : 0f;
                value = hoverProgress;
            } else {
                target = getModel().isPressed() ? 1f : 0f;
                value = pressProgress;
            }
            float next = value + (target - value) * 0.28f;
            if (Math.abs(next - target) < 0.015f) next = target;
            if ("selected".equals(field)) selectedProgress = next;
            else if ("hover".equals(field)) hoverProgress = next;
            else pressProgress = next;
            return next != target;
        }
    }

    private class ClientCard extends GlassPanel {
        private final JLabel title;
        private final JLabel address;
        private final JLabel state;

        ClientCard(String name) {
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(20, 20, 20, 20));
            title = sectionTitle(name);
            address = muted("Waiting");
            state = value("Offline");
            GridBagConstraints c = gbc();
            c.weightx = 1;
            add(title, c);
            c = gbc();
            c.gridy = 1;
            add(address, c);
            c = gbc();
            c.gridy = 2;
            add(state, c);
        }

        void setConnection(ProxyConnection connection, boolean active) {
            address.setText(connection.getRealSrcAddress().getAddress().getHostAddress());
            setActive(active);
        }

        void setActive(boolean active) {
            setHighlighted(active);
            state.setText(active ? "Controller" : "Connected");
            state.setForeground(active ? GOOD : AMETHYST_SOFT);
        }

        void clear() {
            setHighlighted(false);
            address.setText("Waiting");
            state.setText("Offline");
            state.setForeground(TEXT);
        }
    }

    private class AnimatedButton extends JButton {
        private final boolean primary;
        private float hoverProgress;
        private float pressProgress;
        private final Timer animator;

        AnimatedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setForeground(primary ? Color.WHITE : TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getModel().addChangeListener(e -> ensureAnimation());
            animator = new Timer(16, e -> {
                boolean changed = false;
                changed |= approachHover();
                changed |= approachPress();
                if (!changed) ((Timer) e.getSource()).stop();
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offset = Math.round(pressProgress * 2f);
            int alpha = isEnabled() ? 255 : 112;
            Color top = primary ? withAlpha(AMETHYST_SOFT, alpha) : withAlpha(SURFACE_HOVER, alpha);
            Color bottom = primary ? withAlpha(AMETHYST, alpha) : withAlpha(CONTROL, alpha);
            top = mix(top, primary ? withAlpha(AMETHYST_SOFT.brighter(), alpha) : withAlpha(SURFACE_HOVER.brighter(), alpha), hoverProgress);
            g.translate(0, offset);
            g.setColor(new Color(0, 0, 0, isEnabled() ? 70 : 24));
            g.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 5, 14, 14);
            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 14, 14);
            g.setColor(new Color(255, 255, 255, Math.round((primary ? 42 : 22) + hoverProgress * 28)));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 14, 14);
            if (pressProgress > 0.01f) {
                g.setColor(new Color(0, 0, 0, Math.round(42 * pressProgress)));
                g.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 14, 14);
            }
            g.dispose();
            super.paintComponent(graphics);
        }

        private void ensureAnimation() {
            if (!animator.isRunning()) animator.start();
        }

        private boolean approachHover() {
            float target = isEnabled() && getModel().isRollover() ? 1f : 0f;
            float next = hoverProgress + (target - hoverProgress) * 0.25f;
            if (Math.abs(next - target) < 0.015f) next = target;
            boolean changed = next != target;
            hoverProgress = next;
            return changed;
        }

        private boolean approachPress() {
            float target = isEnabled() && getModel().isPressed() ? 1f : 0f;
            float next = pressProgress + (target - pressProgress) * 0.36f;
            if (Math.abs(next - target) < 0.015f) next = target;
            boolean changed = next != target;
            pressProgress = next;
            return changed;
        }
    }

    private class AnimatedCheckBox extends JCheckBox {
        private float selectedProgress;
        private float hoverProgress;
        private final Timer animator;

        AnimatedCheckBox(String text, boolean selected) {
            super(text, selected);
            selectedProgress = selected ? 1f : 0f;
            setOpaque(false);
            setForeground(TEXT);
            setFocusPainted(false);
            setIconTextGap(10);
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setIcon(new EmptyIcon(28, 18));
            setSelectedIcon(new EmptyIcon(28, 18));
            getModel().addChangeListener(e -> ensureAnimation());
            animator = new Timer(16, e -> {
                boolean changed = false;
                changed |= approachSelected();
                changed |= approachHover();
                if (!changed) ((Timer) e.getSource()).stop();
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hoverProgress > 0.01f) {
                g.setColor(new Color(255, 255, 255, Math.round(12 * hoverProgress)));
                g.fillRoundRect(0, 1, getWidth(), getHeight() - 2, 12, 12);
            }
            int box = 18;
            int x = 8;
            int y = (getHeight() - box) / 2;
            g.setPaint(new GradientPaint(x, y, mix(CONTROL, AMETHYST, selectedProgress), x, y + box, withAlpha(GRADIENT_END, 238)));
            g.fillRoundRect(x, y, box, box, 8, 8);
            g.setColor(new Color(255, 255, 255, 58 + Math.round(70 * selectedProgress)));
            g.drawRoundRect(x, y, box, box, 8, 8);
            if (selectedProgress > 0.05f) {
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(255, 255, 255, Math.round(255 * selectedProgress)));
                int mid = x + 7;
                g.drawLine(x + 5, y + 10, mid, y + 13);
                g.drawLine(mid, y + 13, x + 14, y + 6);
            }
            g.dispose();
            super.paintComponent(graphics);
        }

        private void ensureAnimation() {
            if (!animator.isRunning()) animator.start();
        }

        private boolean approachSelected() {
            float target = isSelected() ? 1f : 0f;
            float next = selectedProgress + (target - selectedProgress) * 0.24f;
            if (Math.abs(next - target) < 0.015f) next = target;
            boolean changed = next != target;
            selectedProgress = next;
            return changed;
        }

        private boolean approachHover() {
            float target = getModel().isRollover() ? 1f : 0f;
            float next = hoverProgress + (target - hoverProgress) * 0.22f;
            if (Math.abs(next - target) < 0.015f) next = target;
            boolean changed = next != target;
            hoverProgress = next;
            return changed;
        }
    }

    private static class EmptyIcon implements Icon {
        private final int width;
        private final int height;

        EmptyIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private class AccountRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setBorder(new EmptyBorder(8, 10, 8, 10));
            if (value == null) component.setText(I18n.get("tab.general.minecraft_account.option_no_account"));
            else if (value instanceof Account account) component.setText(account.getDisplayString());
            return component;
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable runnable;

        SimpleDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            runnable.run();
        }
    }

    private void setAlpha(Component component, float alpha) {
        if (component instanceof Page page) page.setPageAlpha(alpha);
    }

    private float easeOutCubic(float value) {
        float inverse = 1f - value;
        return 1f - inverse * inverse * inverse;
    }

    private Color mix(Color from, Color to, float amount) {
        float value = Math.max(0f, Math.min(1f, amount));
        int red = Math.round(from.getRed() + (to.getRed() - from.getRed()) * value);
        int green = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * value);
        int blue = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * value);
        int alpha = Math.round(from.getAlpha() + (to.getAlpha() - from.getAlpha()) * value);
        return new Color(red, green, blue, alpha);
    }

    private Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    private Color parseColor(String value, Color fallback) {
        try {
            return Color.decode(normalizeHexOrThrow(value));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private String normalizeHex(String value, String fallback) {
        try {
            return normalizeHexOrThrow(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private String normalizeHexOrThrow(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (normalized.length() == 3) {
            normalized = "" + normalized.charAt(0) + normalized.charAt(0)
                    + normalized.charAt(1) + normalized.charAt(1)
                    + normalized.charAt(2) + normalized.charAt(2);
        }
        if (!normalized.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Theme color must be HEX like #E5B52D");
        }
        return "#" + normalized.toUpperCase();
    }

    private int brightness(Color color) {
        return Math.round((color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000f);
    }

    private GlassPanel card() {
        GlassPanel panel = new GlassPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    private JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    private JLabel value(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        return label;
    }

    private JButton primaryButton(String text) {
        return new AnimatedButton(text, true);
    }

    private JButton softButton(String text) {
        return new AnimatedButton(text, false);
    }

    private JCheckBox option(String text, boolean selected) {
        return new AnimatedCheckBox(text, selected);
    }

    private JPanel field(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(label(title), BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        return c;
    }
}
