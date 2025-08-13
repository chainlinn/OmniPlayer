package mpv.oneblue3.movieapp_teat.s;

// UIUpdater.java - 更强大的接口
interface UIUpdater {
    void updateUI();
    void updateUI(String action); // 带参数的更新，可以指定更新类型
    void setCustomUpdateCallback(Runnable callback);
    void setPreUpdateCallback(Runnable preCallback); // 更新前的回调
    void setPostUpdateCallback(Runnable postCallback); // 更新后的回调
    void updateUIWithDelay(long delayMs); // 延迟更新
}

// UIMain.java - 模拟Android Activity
public class UIMain {
    private UI0 ui0Fragment;
    private UI1 ui1Fragment;
    private String currentTab = "UI0";
    private Runnable customCallback;
    private Runnable preUpdateCallback;
    private Runnable postUpdateCallback;
    private boolean isUpdating = false; // 防止重复更新

    
    // 内部接口实现
    private UIUpdater uiUpdater = new UIUpdater() {
        @Override
        public void updateUI() {
            updateUI("default");
        }
        
        @Override
        public void updateUI(String action) {
            if (isUpdating) {
                System.out.println("UIMain: 更新正在进行中，跳过重复请求");
                return;
            }
            
            isUpdating = true;
            System.out.println("UIMain(Activity): 当前Tab[" + currentTab + "] 请求UI更新 (动作: " + action + ")");
            
            try {
                // 前置回调
                if (preUpdateCallback != null) {
                    System.out.println("UIMain: 执行前置回调...");
                    preUpdateCallback.run();
                }
                
                System.out.println("UIMain: 执行UI更新操作...");
                
                // Fragment的自定义逻辑
                if (customCallback != null) {
                    System.out.println("UIMain: 执行Fragment的自定义更新逻辑...");
                    customCallback.run();
                }
                
                // Activity的标准更新逻辑
                refreshCurrentTab(action);
                repaintInterface();
                
                // 后置回调
                if (postUpdateCallback != null) {
                    System.out.println("UIMain: 执行后置回调...");
                    postUpdateCallback.run();
                }
                
            } finally {
                isUpdating = false;
            }
        }
        
        @Override
        public void setCustomUpdateCallback(Runnable callback) {
            customCallback = callback;
        }
        
        @Override
        public void setPreUpdateCallback(Runnable preCallback) {
            preUpdateCallback = preCallback;
        }
        
        @Override
        public void setPostUpdateCallback(Runnable postCallback) {
            postUpdateCallback = postCallback;
        }
        
        @Override
        public void updateUIWithDelay(long delayMs) {
            System.out.println("UIMain: 延迟 " + delayMs + "ms 后更新UI");
            // 这里可以用Timer或者Handler实现真正的延迟
            try {
                Thread.sleep(delayMs);
                updateUI("delayed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    };
    
    public UIMain() {
        // 初始化fragments
        ui0Fragment = new UI0(uiUpdater);
        ui1Fragment = new UI1(uiUpdater);
    }
    
    // 模拟切换tab的方法
    public void switchToTab(String tabName) {
        System.out.println("UIMain: 切换到Tab[" + tabName + "]");
        this.currentTab = tabName;
        
        // 模拟fragment切换逻辑
        hideAllFragments();
        showFragment(tabName);
        
        // 切换tab时，更新自定义回调
        updateCustomCallback(tabName);
    }
    
    // 根据当前tab设置对应的自定义回调
    private void updateCustomCallback(String tabName) {
        if ("UI0".equals(tabName)) {
            uiUpdater.setCustomUpdateCallback(ui0Fragment.getCustomUpdateCallback());
            uiUpdater.setPreUpdateCallback(ui0Fragment.getPreUpdateCallback());
            uiUpdater.setPostUpdateCallback(ui0Fragment.getPostUpdateCallback());
        } else if ("UI1".equals(tabName)) {
            uiUpdater.setCustomUpdateCallback(ui1Fragment.getCustomUpdateCallback());
            uiUpdater.setPreUpdateCallback(ui1Fragment.getPreUpdateCallback());
            uiUpdater.setPostUpdateCallback(ui1Fragment.getPostUpdateCallback());
        }
    }
    
    private void hideAllFragments() {
        System.out.println("  - 隐藏所有Fragment");
    }
    
    private void showFragment(String tabName) {
        System.out.println("  - 显示Fragment: " + tabName);
    }
    
    // 根据当前tab执行不同的更新逻辑
    private void refreshCurrentTab(String action) {
        System.out.println("  - 刷新当前Tab[" + currentTab + "]的组件 (动作: " + action + ")");
        if ("UI0".equals(currentTab)) {
            System.out.println("  - 更新UI0特有的视图元素");
            if ("data-refresh".equals(action)) {
                System.out.println("  - UI0: 执行数据刷新");
            }
        } else if ("UI1".equals(currentTab)) {
            System.out.println("  - 更新UI1特有的视图元素");
            if ("list-update".equals(action)) {
                System.out.println("  - UI1: 执行列表更新");
            }
        }
    }
    
    private void repaintInterface() {
        System.out.println("  - 重绘界面");
    }
    
    // 获取当前显示的fragment
    public String getCurrentTab() {
        return currentTab;
    }
    
    // 模拟用户交互触发fragment操作
    public void simulateUI0Action() {
        System.out.println("=== 用户在UI0中执行操作 ===");
        switchToTab("UI0");
        ui0Fragment.performAction();
    }
    
    public void simulateUI1Action() {
        System.out.println("=== 用户在UI1中执行操作 ===");
        switchToTab("UI1");
        ui1Fragment.performAction();
    }
    
    // 获取UIUpdater实例的方法
    public UIUpdater getUpdater() {
        return uiUpdater;
    }
    
    // 主方法用于测试
    public static void main(String[] args) {
        UIMain activity = new UIMain();
        
        // 模拟用户操作
        activity.simulateUI0Action();
        
        System.out.println();
        activity.simulateUI1Action();
        
        System.out.println();
        // 模拟在UI1中再次操作
        System.out.println("=== UI1中的事件处理 ===");
        activity.ui1Fragment.handleEvent();
        
        // 模拟更多复杂的操作
        System.out.println("\n=== 测试带参数的更新 ===");
        activity.ui0Fragment.refreshData();
        
        System.out.println("\n=== 测试延迟更新 ===");
        activity.ui1Fragment.scheduleUpdate();
        
        System.out.println("\n=== 测试防重复更新 ===");
        activity.ui1Fragment.rapidUpdates();
    }
}

// UI0.java - 模拟Fragment
class UI0 {
    private UIUpdater updater;
    
    // Fragment的各种回调
    private Runnable customUpdateCallback = () -> {
        System.out.println("  >> UI0自定义更新: 检查数据完整性");
        System.out.println("  >> UI0自定义更新: 更新特定的UI组件状态");
        System.out.println("  >> UI0自定义更新: 触发动画效果");
    };
    
    private Runnable preUpdateCallback = () -> {
        System.out.println("  >> UI0前置: 保存当前状态");
        System.out.println("  >> UI0前置: 显示加载指示器");
    };
    
    private Runnable postUpdateCallback = () -> {
        System.out.println("  >> UI0后置: 隐藏加载指示器");
        System.out.println("  >> UI0后置: 通知其他组件");
    };
    
    public UI0(UIUpdater updater) {
        this.updater = updater;
    }
    
    // 提供各种回调给Activity
    public Runnable getCustomUpdateCallback() { return customUpdateCallback; }
    public Runnable getPreUpdateCallback() { return preUpdateCallback; }
    public Runnable getPostUpdateCallback() { return postUpdateCallback; }
    
    public void performAction() {
        System.out.println("UI0(Fragment): 执行特定操作...");
        updater.updateUI();
        System.out.println("UI0: 操作完成");
    }
    
    public void doSomethingSpecific() {
        System.out.println("UI0(Fragment): 执行UI0特有的功能");
        updater.updateUI();
    }
    
    // 新增方法：数据刷新
    public void refreshData() {
        System.out.println("UI0(Fragment): 请求刷新数据");
        updater.updateUI("data-refresh");
    }
}

// UI1.java - 模拟Fragment
class UI1 {
    private UIUpdater updater;
    
    // Fragment的各种回调
    private Runnable customUpdateCallback = () -> {
        System.out.println("  >> UI1自定义更新: 刷新列表数据");
        System.out.println("  >> UI1自定义更新: 重新计算布局");
        System.out.println("  >> UI1自定义更新: 更新状态栏");
    };
    
    private Runnable preUpdateCallback = () -> {
        System.out.println("  >> UI1前置: 清空缓存");
        System.out.println("  >> UI1前置: 准备网络请求");
    };
    
    private Runnable postUpdateCallback = () -> {
        System.out.println("  >> UI1后置: 更新缓存");
        System.out.println("  >> UI1后置: 记录操作日志");
    };
    
    public UI1(UIUpdater updater) {
        this.updater = updater;
    }
    
    // 提供各种回调给Activity
    public Runnable getCustomUpdateCallback() { return customUpdateCallback; }
    public Runnable getPreUpdateCallback() { return preUpdateCallback; }
    public Runnable getPostUpdateCallback() { return postUpdateCallback; }
    
    public void performAction() {
        System.out.println("UI1(Fragment): 执行特定操作...");
        updater.updateUI();
        System.out.println("UI1: 操作完成");
    }
    
    public void handleEvent() {
        System.out.println("UI1(Fragment): 处理事件");
        updater.updateUI("list-update");
    }
    
    // 新增方法：延迟更新
    public void scheduleUpdate() {
        System.out.println("UI1(Fragment): 安排延迟更新");
        updater.updateUIWithDelay(100); // 100ms延迟
    }
    
    // 新增方法：快速连续更新（测试防重复）
    public void rapidUpdates() {
        System.out.println("UI1(Fragment): 快速连续更新");
        updater.updateUI("rapid-1");
        updater.updateUI("rapid-2");
        updater.updateUI("rapid-3");
    }
}