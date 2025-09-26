// TiGateway GitBook 风格 JavaScript 功能

document.addEventListener('DOMContentLoaded', function() {
    // 移动端菜单切换
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const sidebar = document.querySelector('.book-sidebar');
    
    if (mobileMenuToggle && sidebar) {
        mobileMenuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
        
        // 点击侧边栏外部关闭菜单
        document.addEventListener('click', function(e) {
            if (!sidebar.contains(e.target) && !mobileMenuToggle.contains(e.target)) {
                sidebar.classList.remove('open');
            }
        });
    }
    
    // 导航组展开/折叠
    const navGroups = document.querySelectorAll('.nav-group');
    navGroups.forEach(function(group) {
        const title = group.querySelector('.nav-group-title');
        if (title) {
            title.addEventListener('click', function() {
                group.classList.toggle('expanded');
                
                // 保存状态到 localStorage
                const isExpanded = group.classList.contains('expanded');
                const groupId = group.querySelector('.nav-group-title span').textContent;
                localStorage.setItem('nav-group-' + groupId, isExpanded);
            });
            
            // 恢复保存的状态
            const groupId = group.querySelector('.nav-group-title span').textContent;
            const savedState = localStorage.getItem('nav-group-' + groupId);
            if (savedState === 'true') {
                group.classList.add('expanded');
            }
        }
    });
    
    // 平滑滚动
    document.querySelectorAll('a[href^="#"]').forEach(function(anchor) {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // 代码块复制功能
    document.querySelectorAll('pre').forEach(function(pre) {
        const button = document.createElement('button');
        button.className = 'copy-button';
        button.innerHTML = '<i class="fas fa-copy"></i> 复制';
        button.style.cssText = `
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
            background: var(--primary-color);
            color: white;
            border: none;
            border-radius: var(--radius-sm);
            padding: 0.25rem 0.5rem;
            font-size: 0.8rem;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.3s ease;
            z-index: 10;
        `;
        
        pre.style.position = 'relative';
        pre.appendChild(button);
        
        // 显示/隐藏复制按钮
        pre.addEventListener('mouseenter', function() {
            button.style.opacity = '1';
        });
        
        pre.addEventListener('mouseleave', function() {
            button.style.opacity = '0';
        });
        
        // 复制功能
        button.addEventListener('click', async function() {
            const code = pre.querySelector('code').textContent;
            try {
                await navigator.clipboard.writeText(code);
                button.innerHTML = '<i class="fas fa-check"></i> 已复制';
                button.style.background = 'var(--accent-color)';
                
                setTimeout(function() {
                    button.innerHTML = '<i class="fas fa-copy"></i> 复制';
                    button.style.background = 'var(--primary-color)';
                }, 2000);
            } catch (err) {
                console.error('复制失败:', err);
                button.innerHTML = '<i class="fas fa-times"></i> 失败';
                button.style.background = '#ef4444';
                
                setTimeout(function() {
                    button.innerHTML = '<i class="fas fa-copy"></i> 复制';
                    button.style.background = 'var(--primary-color)';
                }, 2000);
            }
        });
    });
    
    // 表格响应式处理
    const tables = document.querySelectorAll('table');
    tables.forEach(function(table) {
        const wrapper = document.createElement('div');
        wrapper.style.cssText = `
            overflow-x: auto;
            margin-bottom: var(--spacing-lg);
            border-radius: var(--radius-md);
            border: 1px solid var(--border-color);
        `;
        
        table.parentNode.insertBefore(wrapper, table);
        wrapper.appendChild(table);
    });
    
    // 搜索功能（简单实现）
    const searchInput = document.createElement('input');
    searchInput.type = 'text';
    searchInput.placeholder = '搜索文档...';
    searchInput.style.cssText = `
        width: 100%;
        padding: var(--spacing-sm) var(--spacing-md);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-md);
        background-color: var(--bg-color);
        color: var(--text-color);
        font-size: 0.9rem;
        margin-bottom: var(--spacing-md);
    `;
    
    const nav = document.querySelector('.book-nav');
    if (nav) {
        nav.insertBefore(searchInput, nav.firstChild);
        
        searchInput.addEventListener('input', function() {
            const query = this.value.toLowerCase();
            const navLinks = document.querySelectorAll('.nav-link');
            
            navLinks.forEach(function(link) {
                const text = link.textContent.toLowerCase();
                if (text.includes(query) || query === '') {
                    link.style.display = 'flex';
                } else {
                    link.style.display = 'none';
                }
            });
        });
    }
    
    // 返回顶部按钮
    const scrollTopBtn = document.createElement('button');
    scrollTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
    scrollTopBtn.style.cssText = `
        position: fixed;
        bottom: 2rem;
        right: 2rem;
        width: 3rem;
        height: 3rem;
        background-color: var(--primary-color);
        color: white;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        box-shadow: var(--shadow-lg);
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s ease;
        z-index: 1000;
    `;
    
    document.body.appendChild(scrollTopBtn);
    
    // 显示/隐藏返回顶部按钮
    window.addEventListener('scroll', function() {
        if (window.scrollY > 300) {
            scrollTopBtn.style.opacity = '1';
            scrollTopBtn.style.visibility = 'visible';
        } else {
            scrollTopBtn.style.opacity = '0';
            scrollTopBtn.style.visibility = 'hidden';
        }
    });
    
    // 返回顶部功能
    scrollTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    // 键盘快捷键
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K 打开搜索
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            if (searchInput) {
                searchInput.focus();
            }
        }
        
        // ESC 关闭移动端菜单
        if (e.key === 'Escape') {
            if (sidebar) {
                sidebar.classList.remove('open');
            }
        }
    });
    
    // 主题切换（预留功能）
    const themeToggle = document.createElement('button');
    themeToggle.innerHTML = '<i class="fas fa-moon"></i>';
    themeToggle.title = '切换主题';
    themeToggle.style.cssText = `
        position: fixed;
        top: 1rem;
        right: 1rem;
        width: 2.5rem;
        height: 2.5rem;
        background-color: var(--bg-color);
        color: var(--text-color);
        border: 1px solid var(--border-color);
        border-radius: 50%;
        cursor: pointer;
        box-shadow: var(--shadow-md);
        transition: var(--transition);
        z-index: 1000;
    `;
    
    // 暂时隐藏主题切换按钮，等待后续实现
    // document.body.appendChild(themeToggle);
    
    // 页面加载动画
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.3s ease';
    
    setTimeout(function() {
        document.body.style.opacity = '1';
    }, 100);
    
    // 图片懒加载
    const images = document.querySelectorAll('img[data-src]');
    const imageObserver = new IntersectionObserver(function(entries, observer) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(function(img) {
        imageObserver.observe(img);
    });
    
    // 链接外部链接处理
    document.querySelectorAll('a[href^="http"]').forEach(function(link) {
        if (!link.hostname.includes(window.location.hostname)) {
            link.target = '_blank';
            link.rel = 'noopener noreferrer';
        }
    });
    
    console.log('TiGateway 文档站点已加载完成');
});