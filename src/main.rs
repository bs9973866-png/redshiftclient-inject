#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
mod overlay;
mod platform;
use eframe::egui;
fn main() -> eframe::Result<()> {
    let (tx, rx) = std::sync::mpsc::channel();
    platform::start_hotkey_listener(tx);
    let options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default()
            .with_title("REDSHIFT Overlay")
            .with_inner_size([610.0, 460.0])
            .with_min_inner_size([540.0, 400.0])
            .with_position([100.0, 100.0])
            .with_decorations(false)
            .with_transparent(true)
            .with_window_level(egui::ViewportLevel::AlwaysOnTop),
        ..Default::default()
    };
    eframe::run_native("REDSHIFT Overlay", options,
        Box::new(move |_cc| Ok(Box::new(overlay::RedshiftOverlay::new(rx)))))
}
