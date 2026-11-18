use std::{fs, path::PathBuf, sync::mpsc::Receiver};
use eframe::egui::{self, Align, Color32, FontId, Layout, RichText, Stroke};
use serde::{Deserialize, Serialize};
use crate::platform;

const BG: Color32 = Color32::from_rgb(13, 9, 20);
const CARD: Color32 = Color32::from_rgba_premultiplied(43, 28, 51, 220);
const CRIMSON: Color32 = Color32::from_rgb(255, 55, 83);
const TEXT: Color32 = Color32::from_rgb(249, 239, 246);
const MUTED: Color32 = Color32::from_rgb(168, 151, 171);

#[derive(Clone, Serialize, Deserialize)]
struct OverlayProfile {
    opacity: f32,
    #[serde(default = "default_ui_scale")]
    ui_scale: f32,
    accent_glow: bool,
    follow_game_window: bool,
    show_fps_widget: bool,
    show_status_widget: bool,
    fps_position: [f32; 2],
    status_position: [f32; 2],
    #[serde(default = "default_crosshair_position")]
    crosshair_position: [f32; 2],
    #[serde(default)]
    show_crosshair_widget: bool,
    #[serde(default)]
    widgets_only_with_game: bool,
    #[serde(default)]
    auto_hide_menu_when_unfocused: bool,
    #[serde(default)]
    hide_when_game_minimized: bool,
}

fn default_crosshair_position() -> [f32; 2] { [370.0, 285.0] }
fn default_ui_scale() -> f32 { 1.0 }

impl Default for OverlayProfile {
    fn default() -> Self {
        Self {
            opacity: 0.94,
            ui_scale: 1.0,
            accent_glow: true,
            follow_game_window: true,
            show_fps_widget: true,
            show_status_widget: true,
            fps_position: [370.0, 155.0],
            status_position: [370.0, 220.0],
            crosshair_position: [370.0, 285.0],
            show_crosshair_widget: false,
            widgets_only_with_game: false,
            auto_hide_menu_when_unfocused: false,
            hide_when_game_minimized: false,
        }
    }
}

fn profile_path() -> PathBuf {
    let mut path = std::env::var_os("APPDATA")
        .map(PathBuf::from)
        .unwrap_or_else(std::env::temp_dir);
    path.push("REDSHIFT");
    path.push("profiles");
    path.push("default.json");
    path
}

fn read_profile() -> Option<OverlayProfile> {
    let raw = fs::read_to_string(profile_path()).ok()?;
    serde_json::from_str(&raw).ok()
}

pub struct RedshiftOverlay {
    hotkey_rx: Receiver<()>,
    menu_open: bool,
    selected_tab: usize,
    minecraft_window: Option<platform::MinecraftWindowInfo>,
    follow_game_window: bool,
    last_follow_position: Option<(i32, i32)>,
    last_game_foreground: bool,
    viewport_visible: bool,
    mouse_passthrough: bool,
    opacity: f32,
    ui_scale: f32,
    accent_glow: bool,
    show_fps_widget: bool,
    show_status_widget: bool,
    show_crosshair_widget: bool,
    widgets_only_with_game: bool,
    auto_hide_menu_when_unfocused: bool,
    hide_when_game_minimized: bool,
    fps_position: egui::Pos2,
    status_position: egui::Pos2,
    crosshair_position: egui::Pos2,
    profile_status: String,
}

impl RedshiftOverlay {
    pub fn new(hotkey_rx: Receiver<()>) -> Self {
        let profile = read_profile().unwrap_or_default();
        Self {
            hotkey_rx,
            menu_open: true,
            selected_tab: 0,
            minecraft_window: None,
            follow_game_window: profile.follow_game_window,
            last_follow_position: None,
            last_game_foreground: false,
            viewport_visible: true,
            mouse_passthrough: false,
            opacity: profile.opacity,
            ui_scale: profile.ui_scale.clamp(0.8, 1.3),
            accent_glow: profile.accent_glow,
            show_fps_widget: profile.show_fps_widget,
            show_status_widget: profile.show_status_widget,
            show_crosshair_widget: profile.show_crosshair_widget,
            widgets_only_with_game: profile.widgets_only_with_game,
            auto_hide_menu_when_unfocused: profile.auto_hide_menu_when_unfocused,
            hide_when_game_minimized: profile.hide_when_game_minimized,
            fps_position: egui::pos2(profile.fps_position[0], profile.fps_position[1]),
            status_position: egui::pos2(profile.status_position[0], profile.status_position[1]),
            crosshair_position: egui::pos2(profile.crosshair_position[0], profile.crosshair_position[1]),
            profile_status: "Default profile loaded when available".into(),
        }
    }

    fn profile(&self) -> OverlayProfile {
        OverlayProfile {
            opacity: self.opacity,
            ui_scale: self.ui_scale,
            accent_glow: self.accent_glow,
            follow_game_window: self.follow_game_window,
            show_fps_widget: self.show_fps_widget,
            show_status_widget: self.show_status_widget,
            fps_position: [self.fps_position.x, self.fps_position.y],
            status_position: [self.status_position.x, self.status_position.y],
            crosshair_position: [self.crosshair_position.x, self.crosshair_position.y],
            show_crosshair_widget: self.show_crosshair_widget,
            widgets_only_with_game: self.widgets_only_with_game,
            auto_hide_menu_when_unfocused: self.auto_hide_menu_when_unfocused,
            hide_when_game_minimized: self.hide_when_game_minimized,
        }
    }

    fn save_profile(&mut self) {
        let path = profile_path();
        let result = (|| -> Result<(), String> {
            let parent = path.parent().ok_or("Profile folder is unavailable")?;
            fs::create_dir_all(parent).map_err(|e| e.to_string())?;
            let json = serde_json::to_string_pretty(&self.profile()).map_err(|e| e.to_string())?;
            fs::write(&path, json).map_err(|e| e.to_string())
        })();
        self.profile_status = match result {
            Ok(()) => format!("Saved: {}", path.display()),
            Err(error) => format!("Could not save profile: {error}"),
        };
    }

    fn load_profile(&mut self) {
        if let Some(profile) = read_profile() {
            self.apply_profile(profile);
            self.profile_status = "Profile loaded".into();
        } else {
            self.profile_status = "No saved profile found yet".into();
        }
    }

    fn apply_profile(&mut self, profile: OverlayProfile) {
        self.opacity = profile.opacity.clamp(0.55, 1.0);
        self.ui_scale = profile.ui_scale.clamp(0.8, 1.3);
        self.accent_glow = profile.accent_glow;
        self.follow_game_window = profile.follow_game_window;
        self.show_fps_widget = profile.show_fps_widget;
        self.show_status_widget = profile.show_status_widget;
        self.fps_position = egui::pos2(profile.fps_position[0], profile.fps_position[1]);
        self.status_position = egui::pos2(profile.status_position[0], profile.status_position[1]);
        self.crosshair_position = egui::pos2(profile.crosshair_position[0], profile.crosshair_position[1]);
        self.show_crosshair_widget = profile.show_crosshair_widget;
        self.widgets_only_with_game = profile.widgets_only_with_game;
        self.auto_hide_menu_when_unfocused = profile.auto_hide_menu_when_unfocused;
        self.hide_when_game_minimized = profile.hide_when_game_minimized;
    }

    fn poll(&mut self, ctx: &egui::Context) {
        while self.hotkey_rx.try_recv().is_ok() {
            self.menu_open = !self.menu_open;
        }
        self.minecraft_window = platform::find_minecraft_window();
        let should_show_viewport = !self.hide_when_game_minimized
            || !self.minecraft_window.as_ref().is_some_and(|game| game.is_minimized);
        if should_show_viewport != self.viewport_visible {
            ctx.send_viewport_cmd(egui::ViewportCommand::Visible(should_show_viewport));
            self.viewport_visible = should_show_viewport;
        }
        let game_is_active = self.minecraft_window.as_ref().is_some_and(|game| game.is_foreground);
        if self.auto_hide_menu_when_unfocused && self.last_game_foreground && !game_is_active {
            self.menu_open = false;
        }
        self.last_game_foreground = game_is_active;
        if self.follow_game_window {
            if let Some(game) = self.minecraft_window.as_ref()
                .filter(|game| game.is_foreground && game.width > 0 && game.height > 0)
            {
                // Windows APIs return physical pixels under per-monitor DPI awareness.
                // egui viewport positions are logical points, so convert using the
                // overlay's current monitor scale before asking winit to move it.
                let target_scale = (game.dpi_scale * self.ui_scale).max(0.5);
                let panel_width_px = (610.0 * target_scale).round() as i32;
                let panel_height_px = (460.0 * target_scale).round() as i32;
                let margin_px = (24.0 * target_scale).round() as i32;
                let min_x = game.work_left;
                let min_y = game.work_top;
                let max_x = (game.work_right - panel_width_px).max(min_x);
                let max_y = (game.work_bottom - panel_height_px).max(min_y);
                let target = (
                    (game.left + margin_px).clamp(min_x, max_x),
                    (game.top + margin_px).clamp(min_y, max_y),
                );
                if self.last_follow_position != Some(target) {
                    let current_scale = (ctx.native_pixels_per_point().unwrap_or(1.0) * self.ui_scale).max(0.5);
                    ctx.send_viewport_cmd(egui::ViewportCommand::OuterPosition(
                        egui::pos2(
                            target.0 as f32 / current_scale,
                            target.1 as f32 / current_scale,
                        ),
                    ));
                    self.last_follow_position = Some(target);
                }
            }
        }
        ctx.request_repaint_after(std::time::Duration::from_millis(180));
    }

    fn card(ui: &mut egui::Ui, title: &str, subtitle: &str, enabled: &mut bool) {
        egui::Frame::new().fill(CARD)
            .stroke(Stroke::new(1.0, Color32::from_rgba_unmultiplied(255, 105, 132, 65)))
            .corner_radius(egui::CornerRadius::same(12)).inner_margin(12.0)
            .show(ui, |ui| ui.horizontal(|ui| {
                ui.vertical(|ui| {
                    ui.label(RichText::new(title).color(TEXT).strong());
                    ui.label(RichText::new(subtitle).size(11.0).color(MUTED));
                });
                ui.with_layout(Layout::right_to_left(Align::Center), |ui| {
                    ui.toggle_value(enabled, "");
                });
            }));
    }

    fn draw_hud_preview(&mut self, ctx: &egui::Context) {
        if self.widgets_only_with_game
            && !self.minecraft_window.as_ref().is_some_and(|game| game.is_foreground)
        {
            return;
        }

        if self.show_fps_widget {
            let area = egui::Area::new(egui::Id::new("redshift-hud-fps"))
                .default_pos(self.fps_position)
                .movable(true)
                .constrain(true)
                .show(ctx, |ui| {
                    egui::Frame::new().fill(CARD)
                        .stroke(Stroke::new(1.0, CRIMSON))
                        .corner_radius(egui::CornerRadius::same(10))
                        .inner_margin(egui::Margin::symmetric(12, 8))
                        .show(ui, |ui| {
                            ui.label(RichText::new("FPS  •  PREVIEW").strong().color(TEXT));
                            ui.label(RichText::new("Game data not connected").size(10.0).color(MUTED));
                        });
                });
            self.fps_position = area.response.rect.min;
        }

        if self.show_status_widget {
            let area = egui::Area::new(egui::Id::new("redshift-hud-status"))
                .default_pos(self.status_position)
                .movable(true)
                .constrain(true)
                .show(ctx, |ui| {
                    egui::Frame::new().fill(CARD)
                        .stroke(Stroke::new(1.0, Color32::from_rgba_unmultiplied(255, 105, 132, 90)))
                        .corner_radius(egui::CornerRadius::same(10))
                        .inner_margin(egui::Margin::symmetric(12, 8))
                        .show(ui, |ui| {
                            ui.label(RichText::new("REDSHIFT").strong().color(TEXT));
                            ui.label(RichText::new(if self.minecraft_window.is_some() {
                                "Minecraft detected"
                            } else {
                                "Waiting for Minecraft"
                            }).size(10.0).color(MUTED));
                        });
                });
            self.status_position = area.response.rect.min;
        }

        if self.show_crosshair_widget {
            let area = egui::Area::new(egui::Id::new("redshift-hud-crosshair"))
                .default_pos(self.crosshair_position)
                .movable(true)
                .constrain(true)
                .show(ctx, |ui| {
                    egui::Frame::new().fill(CARD)
                        .stroke(Stroke::new(1.0, Color32::from_rgba_unmultiplied(255, 105, 132, 90)))
                        .corner_radius(egui::CornerRadius::same(10))
                        .inner_margin(egui::Margin::symmetric(10, 6))
                        .show(ui, |ui| {
                            ui.label(RichText::new("CROSSHAIR PREVIEW").size(9.0).color(MUTED));
                            let (rect, _) = ui.allocate_exact_size(egui::vec2(76.0, 42.0), egui::Sense::hover());
                            let center = rect.center();
                            let painter = ui.painter();
                            let stroke = Stroke::new(2.0, Color32::WHITE);
                            let gap = 4.0;
                            let arm = 10.0;
                            painter.line_segment([egui::pos2(center.x - gap - arm, center.y), egui::pos2(center.x - gap, center.y)], stroke);
                            painter.line_segment([egui::pos2(center.x + gap, center.y), egui::pos2(center.x + gap + arm, center.y)], stroke);
                            painter.line_segment([egui::pos2(center.x, center.y - gap - arm), egui::pos2(center.x, center.y - gap)], stroke);
                            painter.line_segment([egui::pos2(center.x, center.y + gap), egui::pos2(center.x, center.y + gap + arm)], stroke);
                        });
                });
            self.crosshair_position = area.response.rect.min;
        }
    }

    fn content(&mut self, ui: &mut egui::Ui) {
        match self.selected_tab {
            0 => {
                ui.label(RichText::new("Welcome back.").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Standalone overlay • no Minecraft mod loader required").color(MUTED));
                if let Some(game) = self.minecraft_window.as_ref() {
                    ui.add_space(8.0);
                    ui.label(RichText::new(format!("WINDOW  •  {} × {}", game.width, game.height)).size(10.0).color(MUTED));
                    ui.label(RichText::new(format!("DISPLAY DPI  •  {}% ({} DPI)", (game.dpi_scale * 100.0).round() as u32, game.dpi)).size(10.0).color(MUTED));
                    ui.label(RichText::new(game.title.chars().take(52).collect::<String>()).size(11.0).color(TEXT));
                }
                ui.add_space(14.0);
                Self::card(ui, "Status widget", "Draggable desktop HUD preview", &mut self.show_status_widget);
                ui.add_space(8.0);
                Self::card(ui, "FPS widget", "Visual preview only — not real game FPS", &mut self.show_fps_widget);
            }
            1 => {
                ui.label(RichText::new("HUD EDITOR").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Drag the floating widgets to arrange your desktop overlay.").color(MUTED));
                ui.add_space(8.0);
                ui.checkbox(&mut self.show_status_widget, "Show status widget");
                ui.checkbox(&mut self.show_fps_widget, "Show FPS preview widget");
                ui.checkbox(&mut self.show_crosshair_widget, "Show crosshair preview (not in-game)");
                ui.add_space(8.0);
                if ui.button("Reset widget positions").clicked() {
                    self.fps_position = egui::pos2(370.0, 155.0);
                    self.status_position = egui::pos2(370.0, 220.0);
                    self.crosshair_position = egui::pos2(370.0, 285.0);
                }
                ui.label(RichText::new("Widgets are external desktop overlays; game telemetry is not connected.").size(10.0).color(MUTED));
            }
            2 => {
                ui.label(RichText::new("VISUAL SYSTEM").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Black / Crimson / Liquid Glass").color(MUTED));
                ui.add_space(18.0);
                ui.checkbox(&mut self.accent_glow, "Crimson edge highlights");
                ui.add_space(12.0);
                ui.label(RichText::new("Panel opacity").color(TEXT));
                ui.add(egui::Slider::new(&mut self.opacity, 0.55..=1.0));
                ui.add_space(10.0);
                ui.label(RichText::new("Interface scale").color(TEXT));
                ui.add(egui::Slider::new(&mut self.ui_scale, 0.8..=1.3).text("Scale"));
                ui.label(RichText::new("Applies to the whole overlay window.").size(10.0).color(MUTED));
                egui::Frame::new()
                    .fill(Color32::from_rgba_unmultiplied(65, 35, 66, (self.opacity * 230.0) as u8))
                    .stroke(Stroke::new(1.0, CRIMSON))
                    .corner_radius(14)
                    .inner_margin(18.0)
                    .show(ui, |ui| {
                        ui.label(RichText::new("LIQUID GLASS").strong().color(TEXT));
                        ui.label(RichText::new("Layered translucent surface").color(MUTED));
                    });
            }
            3 => {
                ui.label(RichText::new("PROFILES").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("One local JSON profile is stored on this PC.").color(MUTED));
                ui.add_space(12.0);
                if ui.button("Save current profile").clicked() {
                    self.save_profile();
                }
                if ui.button("Load saved profile").clicked() {
                    self.load_profile();
                }
                ui.add_space(8.0);
                ui.label(RichText::new(&self.profile_status).size(10.0).color(MUTED));
                ui.label(RichText::new(profile_path().display().to_string()).size(9.0).color(MUTED));
            }
            _ => {
                ui.label(RichText::new("SETTINGS").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Global hotkey: Right Shift").color(TEXT));
                ui.add_space(10.0);
                ui.checkbox(&mut self.follow_game_window, "Follow Minecraft window");
                ui.label(RichText::new("Moves the panel near the game when Minecraft is focused.").size(11.0).color(MUTED));
                ui.add_space(8.0);
                ui.checkbox(&mut self.widgets_only_with_game, "Show HUD widgets only while Minecraft is focused");
                ui.label(RichText::new("When enabled, desktop preview widgets disappear while another app is active.").size(10.0).color(MUTED));
                ui.add_space(8.0);
                ui.checkbox(&mut self.auto_hide_menu_when_unfocused, "Auto-hide menu when Minecraft loses focus");
                ui.label(RichText::new("Right Shift can still reopen the menu at any time.").size(10.0).color(MUTED));
                ui.label(RichText::new("With the menu closed, mouse clicks pass through to Minecraft. Open the menu to move HUD previews.").size(10.0).color(MUTED));
                ui.add_space(8.0);
                ui.checkbox(&mut self.hide_when_game_minimized, "Hide overlay when Minecraft is minimized");
                ui.label(RichText::new("The whole transparent overlay window is hidden until Minecraft is restored.").size(10.0).color(MUTED));
                ui.add_space(10.0);
                ui.label(RichText::new("Transparent always-on-top window").color(MUTED));
                ui.label(RichText::new("Visible to normal screen capture").color(MUTED));
            }
        }
    }
}

impl eframe::App for RedshiftOverlay {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        // Preserve the OS-provided per-monitor DPI scale and apply the user's
        // interface zoom on top of it.
        ctx.set_zoom_factor(self.ui_scale);
        self.poll(ctx);

        // When the menu is hidden, let mouse clicks pass through the transparent
        // desktop window so Minecraft remains playable underneath it.
        let should_passthrough = !self.menu_open;
        if should_passthrough != self.mouse_passthrough {
            ctx.send_viewport_cmd(egui::ViewportCommand::MousePassthrough(should_passthrough));
            self.mouse_passthrough = should_passthrough;
        }

        // HUD preview widgets are independent of the settings panel. Right Shift
        // hides the menu, but enabled desktop widgets remain visible.
        self.draw_hud_preview(ctx);

        if !self.menu_open {
            egui::CentralPanel::default().frame(egui::Frame::NONE).show(ctx, |ui| {
                ui.allocate_space(egui::vec2(1.0, 1.0));
            });
            return;
        }

        egui::CentralPanel::default().frame(
            egui::Frame::new()
                .fill(Color32::from_rgba_unmultiplied(BG.r(), BG.g(), BG.b(), (255.0 * self.opacity) as u8))
                .stroke(Stroke::new(1.0, Color32::from_rgba_unmultiplied(255, 83, 111, 100)))
                .corner_radius(egui::CornerRadius::same(16))
                .inner_margin(16.0)
        ).show(ctx, |ui| {
            ui.horizontal(|ui| {
                ui.vertical(|ui| {
                    ui.label(RichText::new("REDSHIFT").font(FontId::proportional(22.0)).strong().color(TEXT));
                    ui.label(RichText::new("INJECT CLIENT / EXTERNAL OVERLAY").size(9.0).color(MUTED));
                });
                ui.with_layout(Layout::right_to_left(Align::Center), |ui| {
                    if ui.button(RichText::new("×").size(20.0).color(TEXT)).clicked() {
                        self.menu_open = false;
                    }
                    let detected = self.minecraft_window.is_some();
                    let status = if detected { "MINECRAFT DETECTED" } else { "WAITING FOR GAME" };
                    ui.label(RichText::new(status).size(9.0).color(if detected {
                        Color32::from_rgb(108, 226, 166)
                    } else {
                        MUTED
                    }));
                });
            });
            ui.add_space(8.0);
            ui.separator();
            ui.add_space(8.0);
            ui.horizontal_top(|ui| {
                ui.vertical(|ui| {
                    for (i, name) in ["Dashboard", "HUD", "Visuals", "Profiles", "Settings"].iter().enumerate() {
                        let selected = self.selected_tab == i;
                        let button = egui::Button::new(
                            RichText::new(*name).color(if selected { TEXT } else { MUTED }).strong()
                        )
                        .fill(if selected {
                            Color32::from_rgba_unmultiplied(255, 55, 83, 48)
                        } else {
                            Color32::TRANSPARENT
                        })
                        .stroke(if selected { Stroke::new(1.0, CRIMSON) } else { Stroke::NONE })
                        .corner_radius(8);
                        if ui.add_sized([118.0, 34.0], button).clicked() {
                            self.selected_tab = i;
                        }
                    }
                });
                ui.separator();
                ui.vertical(|ui| {
                    ui.set_min_width(ui.available_width());
                    self.content(ui);
                });
            });
            ui.with_layout(Layout::bottom_up(Align::LEFT), |ui| {
                ui.label(RichText::new("REDSHIFT • BUILD 0.2.0 • RIGHT SHIFT TO TOGGLE").size(9.0).color(MUTED));
            });
        });

        if self.accent_glow {
            let rect = ctx.available_rect();
            ctx.layer_painter(egui::LayerId::new(egui::Order::Foreground, egui::Id::new("redshift-glow")))
                .line_segment([rect.left_top(), rect.right_top()], Stroke::new(1.0, CRIMSON));
        }
    }
}
