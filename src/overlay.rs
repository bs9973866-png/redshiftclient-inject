use std::sync::mpsc::Receiver;
use eframe::egui::{self, Align, Color32, FontId, Layout, RichText, Stroke};
use crate::platform;

const BG: Color32 = Color32::from_rgb(13, 9, 20);
const CARD: Color32 = Color32::from_rgba_premultiplied(43, 28, 51, 220);
const CRIMSON: Color32 = Color32::from_rgb(255, 55, 83);
const TEXT: Color32 = Color32::from_rgb(249, 239, 246);
const MUTED: Color32 = Color32::from_rgb(168, 151, 171);

pub struct RedshiftOverlay {
    hotkey_rx: Receiver<()>,
    menu_open: bool,
    selected_tab: usize,
    minecraft_found: bool,
    opacity: f32,
    accent_glow: bool,
    show_fps_widget: bool,
    show_crosshair_widget: bool,
    show_status_widget: bool,
}

impl RedshiftOverlay {
    pub fn new(hotkey_rx: Receiver<()>) -> Self {
        Self { hotkey_rx, menu_open: true, selected_tab: 0, minecraft_found: false,
            opacity: 0.94, accent_glow: true, show_fps_widget: false,
            show_crosshair_widget: false, show_status_widget: true }
    }

    fn poll(&mut self, ctx: &egui::Context) {
        while self.hotkey_rx.try_recv().is_ok() { self.menu_open = !self.menu_open; }
        self.minecraft_found = platform::find_minecraft_window().is_some();
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
                ui.with_layout(Layout::right_to_left(Align::Center), |ui| { ui.toggle_value(enabled, ""); });
            }));
    }

    fn content(&mut self, ui: &mut egui::Ui) {
        match self.selected_tab {
            0 | 1 => {
                ui.label(RichText::new(if self.selected_tab == 0 { "Welcome back." } else { "HUD WORKSPACE" }).size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Independent desktop widgets. Game telemetry is not connected yet.").color(MUTED));
                ui.add_space(14.0);
                Self::card(ui, "Status widget", "Local overlay status panel", &mut self.show_status_widget);
                ui.add_space(8.0);
                Self::card(ui, "FPS widget (preview)", "Preview only — no game telemetry source", &mut self.show_fps_widget);
                ui.add_space(8.0);
                Self::card(ui, "Crosshair widget (preview)", "Desktop overlay element, not drawn into Minecraft", &mut self.show_crosshair_widget);
            }
            2 => {
                ui.label(RichText::new("VISUAL SYSTEM").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Black / Crimson / Liquid Glass").color(MUTED));
                ui.add_space(18.0);
                ui.checkbox(&mut self.accent_glow, "Crimson edge highlights");
                ui.add_space(12.0);
                ui.label(RichText::new("Panel opacity").color(TEXT));
                ui.add(egui::Slider::new(&mut self.opacity, 0.55..=1.0));
                egui::Frame::new().fill(Color32::from_rgba_unmultiplied(65, 35, 66, (self.opacity * 230.0) as u8))
                    .stroke(Stroke::new(1.0, CRIMSON)).corner_radius(14).inner_margin(18.0)
                    .show(ui, |ui| { ui.label(RichText::new("LIQUID GLASS").strong().color(TEXT)); ui.label(RichText::new("Layered translucent surface").color(MUTED)); });
            }
            3 => {
                ui.label(RichText::new("PROFILES").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Persistent profile storage is planned for the next milestone.").color(MUTED));
            }
            _ => {
                ui.label(RichText::new("SETTINGS").size(19.0).strong().color(TEXT));
                ui.label(RichText::new("Global hotkey: Right Shift").color(TEXT));
                ui.label(RichText::new("Transparent always-on-top window").color(MUTED));
                ui.label(RichText::new("Visible to normal screen capture").color(MUTED));
            }
        }
    }
}

impl eframe::App for RedshiftOverlay {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        self.poll(ctx);
        if !self.menu_open {
            egui::CentralPanel::default().frame(egui::Frame::NONE).show(ctx, |ui| { ui.allocate_space(egui::vec2(1.0, 1.0)); });
            return;
        }
        egui::CentralPanel::default().frame(
            egui::Frame::new()
                .fill(Color32::from_rgba_unmultiplied(BG.r(), BG.g(), BG.b(), (255.0 * self.opacity) as u8))
                .stroke(Stroke::new(1.0, Color32::from_rgba_unmultiplied(255, 83, 111, 100)))
                .corner_radius(egui::CornerRadius::same(16)).inner_margin(16.0)
        ).show(ctx, |ui| {
            ui.horizontal(|ui| {
                ui.vertical(|ui| {
                    ui.label(RichText::new("REDSHIFT").font(FontId::proportional(22.0)).strong().color(TEXT));
                    ui.label(RichText::new("INJECT CLIENT / EXTERNAL OVERLAY").size(9.0).color(MUTED));
                });
                ui.with_layout(Layout::right_to_left(Align::Center), |ui| {
                    if ui.button(RichText::new("×").size(20.0).color(TEXT)).clicked() { self.menu_open = false; }
                    let status = if self.minecraft_found { "MINECRAFT DETECTED" } else { "WAITING FOR GAME" };
                    ui.label(RichText::new(status).size(9.0).color(if self.minecraft_found { Color32::from_rgb(108, 226, 166) } else { MUTED }));
                });
            });
            ui.add_space(8.0);
            ui.separator();
            ui.add_space(8.0);
            ui.horizontal_top(|ui| {
                ui.vertical(|ui| {
                    for (i, name) in ["Dashboard", "HUD", "Visuals", "Profiles", "Settings"].iter().enumerate() {
                        let selected = self.selected_tab == i;
                        let b = egui::Button::new(RichText::new(*name).color(if selected { TEXT } else { MUTED }).strong())
                            .fill(if selected { Color32::from_rgba_unmultiplied(255, 55, 83, 48) } else { Color32::TRANSPARENT })
                            .stroke(if selected { Stroke::new(1.0, CRIMSON) } else { Stroke::NONE }).corner_radius(8);
                        if ui.add_sized([118.0, 34.0], b).clicked() { self.selected_tab = i; }
                    }
                });
                ui.separator();
                ui.vertical(|ui| { ui.set_min_width(ui.available_width()); self.content(ui); });
            });
            ui.with_layout(Layout::bottom_up(Align::LEFT), |ui| {
                ui.label(RichText::new("REDSHIFT • BUILD 0.1.0 • RIGHT SHIFT TO TOGGLE").size(9.0).color(MUTED));
            });
        });
        if self.accent_glow {
            let rect = ctx.available_rect();
            ctx.layer_painter(egui::LayerId::new(egui::Order::Foreground, egui::Id::new("redshift-glow")))
                .line_segment([rect.left_top(), rect.right_top()], Stroke::new(1.0, CRIMSON));
        }
    }
}
