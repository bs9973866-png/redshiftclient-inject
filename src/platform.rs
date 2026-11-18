use std::{sync::mpsc::Sender, thread, time::Duration};
use windows_sys::Win32::{
    Foundation::{HWND, LPARAM, BOOL, RECT},
    Graphics::Gdi::{GetMonitorInfoW, MonitorFromWindow, MONITORINFO, MONITOR_DEFAULTTONEAREST},
    UI::HiDpi::{
        GetDpiForWindow, SetProcessDpiAwarenessContext,
        DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE,
        DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2,
    },
    UI::{
        Input::KeyboardAndMouse::{GetAsyncKeyState, VK_RSHIFT},
        WindowsAndMessaging::{
            EnumWindows, GetForegroundWindow, GetWindowRect, GetWindowTextW, IsIconic, IsWindowVisible,
        },
    },
};

#[derive(Clone, Debug)]
pub struct MinecraftWindowInfo {
    pub title: String,
    pub left: i32,
    pub top: i32,
    pub width: i32,
    pub height: i32,
    pub is_foreground: bool,
    pub is_minimized: bool,
    pub dpi: u32,
    pub dpi_scale: f32,
    pub work_left: i32,
    pub work_top: i32,
    pub work_right: i32,
    pub work_bottom: i32,
}

pub fn enable_per_monitor_dpi_awareness() {
    // Prefer Per-Monitor V2 (Windows 10 1703+), then fall back to V1.
    // A false result can also mean a manifest or the windowing backend
    // has already selected a process DPI mode.
    unsafe {
        if SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2) == 0 {
            let _ = SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE);
        }
    }
}

pub fn start_hotkey_listener(sender: Sender<()>) {
    thread::Builder::new().name("redshift-hotkey".into()).spawn(move || {
        let mut was_down = false;
        loop {
            let down = unsafe { GetAsyncKeyState(VK_RSHIFT as i32) } < 0;
            if down && !was_down { let _ = sender.send(()); }
            was_down = down;
            thread::sleep(Duration::from_millis(18));
        }
    }).expect("failed to start global hotkey listener");
}

pub fn minecraft_is_foreground() -> bool {
    let hwnd = unsafe { GetForegroundWindow() };
    !hwnd.is_null() && window_title(hwnd).to_lowercase().contains("minecraft")
}

pub fn find_minecraft_window() -> Option<MinecraftWindowInfo> {
    struct Search { found: HWND }
    unsafe extern "system" fn callback(hwnd: HWND, lparam: LPARAM) -> BOOL {
        if IsWindowVisible(hwnd) == 0 { return 1; }
        let state = &mut *(lparam as *mut Search);
        if window_title(hwnd).to_lowercase().contains("minecraft") {
            state.found = hwnd;
            return 0;
        }
        1
    }

    let mut search = Search { found: std::ptr::null_mut() };
    unsafe { EnumWindows(Some(callback), &mut search as *mut Search as LPARAM); }
    if search.found.is_null() { return None; }

    let mut rect: RECT = unsafe { std::mem::zeroed() };
    if unsafe { GetWindowRect(search.found, &mut rect) } == 0 { return None; }

    let foreground = unsafe { GetForegroundWindow() };
    let monitor = unsafe { MonitorFromWindow(search.found, MONITOR_DEFAULTTONEAREST) };
    let mut monitor_info: MONITORINFO = unsafe { std::mem::zeroed() };
    monitor_info.cbSize = std::mem::size_of::<MONITORINFO>() as u32;
    let has_work_area = !monitor.is_null()
        && unsafe { GetMonitorInfoW(monitor, &mut monitor_info) } != 0;
    let (work_left, work_top, work_right, work_bottom) = if has_work_area {
        (
            monitor_info.rcWork.left,
            monitor_info.rcWork.top,
            monitor_info.rcWork.right,
            monitor_info.rcWork.bottom,
        )
    } else {
        (rect.left, rect.top, rect.right, rect.bottom)
    };
    let dpi = unsafe { GetDpiForWindow(search.found) };
    let dpi = if dpi == 0 { 96 } else { dpi };
    Some(MinecraftWindowInfo {
        title: window_title(search.found),
        left: rect.left,
        top: rect.top,
        width: rect.right - rect.left,
        height: rect.bottom - rect.top,
        is_foreground: foreground == search.found,
        is_minimized: unsafe { IsIconic(search.found) != 0 },
        dpi,
        dpi_scale: dpi as f32 / 96.0,
        work_left,
        work_top,
        work_right,
        work_bottom,
    })
}

fn window_title(hwnd: HWND) -> String {
    let mut buffer = [0u16; 512];
    let len = unsafe { GetWindowTextW(hwnd, buffer.as_mut_ptr(), buffer.len() as i32) };
    if len <= 0 { String::new() } else { String::from_utf16_lossy(&buffer[..len as usize]) }
}
