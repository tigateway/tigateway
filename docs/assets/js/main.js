// TiGateway 文档站点主要 JavaScript 功能

document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initNavigation();
    initSearch();
    initCodeCopy();
    initTableOfContents();
    initScrollToTop();
    initThemeToggle();
});

// 导航功能
function initNavigation() {
    // 移动端菜单切换
    const navbarBurger = document.querySelector('.navbar-burger');
    const navbarMenu = document.querySelector('.navbar-menu');
    
    if (navbarBurger && navbarMenu) {
        navbarBurger.addEventListener('click', function() {
            navbarBurger.classList.toggle('is-active');
            navbarMenu.classList.toggle('is-active');
        });
    }
    
    // 侧边栏导航高亮当前页面
    highlightCurrentPage();
    
    // 平滑滚动
    initSmoothScroll();
}

// 高亮当前页面
function highlightCurrentPage() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.menu-list a');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('is-active');
            link.style.color = 'var(--primary-color)';
            link.style.fontWeight = '600';
        }
    });
}

// 平滑滚动
function initSmoothScroll() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// 搜索功能
function initSearch() {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;
    
    // 搜索索引（这里需要根据实际内容生成）
    const searchIndex = generateSearchIndex();
    
    searchInput.addEventListener('input', function() {
        const query = this.value.toLowerCase().trim();
        
        if (query.length < 2) {
            hideSearchResults();
            return;
        }
        
        const results = searchContent(query, searchIndex);
        showSearchResults(results, query);
    });
    
    // 点击外部隐藏搜索结果
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.search-box')) {
            hideSearchResults();
        }
    });
}

// 生成搜索索引
function generateSearchIndex() {
    const index = [];
    const contentElements = document.querySelectorAll('.page-content h1, .page-content h2, .page-content h3, .page-content p');
    
    contentElements.forEach(element => {
        const text = element.textContent.trim();
        if (text.length > 10) {
            index.push({
                text: text,
                element: element,
                type: element.tagName.toLowerCase()
            });
        }
    });
    
    return index;
}

// 搜索内容
function searchContent(query, index) {
    return index.filter(item => 
        item.text.toLowerCase().includes(query)
    ).slice(0, 10); // 限制结果数量
}

// 显示搜索结果
function showSearchResults(results, query) {
    hideSearchResults();
    
    if (results.length === 0) {
        return;
    }
    
    const searchBox = document.querySelector('.search-box');
    const resultsContainer = document.createElement('div');
    resultsContainer.className = 'search-results';
    resultsContainer.innerHTML = `
        <div class="search-results-header">
            <strong>找到 ${results.length} 个结果</strong>
        </div>
        <div class="search-results-list">
            ${results.map(result => `
                <div class="search-result-item" data-target="${result.element.id || ''}">
                    <div class="search-result-title">${highlightText(result.text, query)}</div>
                    <div class="search-result-type">${result.type.toUpperCase()}</div>
                </div>
            `).join('')}
        </div>
    `;
    
    // 添加点击事件
    resultsContainer.addEventListener('click', function(e) {
        const item = e.target.closest('.search-result-item');
        if (item) {
            const targetId = item.dataset.target;
            if (targetId) {
                const targetElement = document.getElementById(targetId);
                if (targetElement) {
                    targetElement.scrollIntoView({ behavior: 'smooth' });
                }
            }
            hideSearchResults();
        }
    });
    
    searchBox.appendChild(resultsContainer);
}

// 隐藏搜索结果
function hideSearchResults() {
    const existingResults = document.querySelector('.search-results');
    if (existingResults) {
        existingResults.remove();
    }
}

// 高亮搜索文本
function highlightText(text, query) {
    const regex = new RegExp(`(${query})`, 'gi');
    return text.replace(regex, '<mark>$1</mark>');
}

// 代码复制功能
function initCodeCopy() {
    const codeBlocks = document.querySelectorAll('pre code');
    
    codeBlocks.forEach(block => {
        const pre = block.parentElement;
        
        // 创建复制按钮
        const copyBtn = document.createElement('button');
        copyBtn.className = 'code-copy-btn';
        copyBtn.textContent = '复制';
        copyBtn.title = '复制代码';
        
        // 添加复制功能
        copyBtn.addEventListener('click', function() {
            const text = block.textContent;
            
            if (navigator.clipboard) {
                navigator.clipboard.writeText(text).then(() => {
                    showCopySuccess(copyBtn);
                }).catch(() => {
                    fallbackCopyText(text, copyBtn);
                });
            } else {
                fallbackCopyText(text, copyBtn);
            }
        });
        
        pre.style.position = 'relative';
        pre.appendChild(copyBtn);
    });
}

// 备用复制方法
function fallbackCopyText(text, button) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        document.execCommand('copy');
        showCopySuccess(button);
    } catch (err) {
        console.error('复制失败:', err);
    }
    
    document.body.removeChild(textArea);
}

// 显示复制成功
function showCopySuccess(button) {
    const originalText = button.textContent;
    button.textContent = '已复制!';
    button.style.backgroundColor = '#28a745';
    
    setTimeout(() => {
        button.textContent = originalText;
        button.style.backgroundColor = '';
    }, 2000);
}

// 目录生成
function initTableOfContents() {
    const content = document.querySelector('.page-content');
    if (!content) return;
    
    const headings = content.querySelectorAll('h1, h2, h3, h4, h5, h6');
    if (headings.length < 2) return;
    
    // 为标题添加 ID
    headings.forEach(heading => {
        if (!heading.id) {
            heading.id = generateHeadingId(heading.textContent);
        }
    });
    
    // 生成目录
    const toc = generateTOC(headings);
    if (toc) {
        insertTOC(toc);
    }
}

// 生成标题 ID
function generateHeadingId(text) {
    return text
        .toLowerCase()
        .replace(/[^\w\s-]/g, '')
        .replace(/\s+/g, '-')
        .trim();
}

// 生成目录
function generateTOC(headings) {
    const toc = document.createElement('div');
    toc.className = 'table-of-contents';
    toc.innerHTML = '<h3>目录</h3><ul></ul>';
    
    const tocList = toc.querySelector('ul');
    let currentLevel = 0;
    
    headings.forEach(heading => {
        const level = parseInt(heading.tagName.charAt(1));
        const item = document.createElement('li');
        item.className = `toc-level-${level}`;
        
        const link = document.createElement('a');
        link.href = `#${heading.id}`;
        link.textContent = heading.textContent;
        link.addEventListener('click', function(e) {
            e.preventDefault();
            heading.scrollIntoView({ behavior: 'smooth' });
        });
        
        item.appendChild(link);
        
        if (level > currentLevel) {
            // 创建新的嵌套列表
            const nestedList = document.createElement('ul');
            nestedList.appendChild(item);
            tocList.appendChild(nestedList);
        } else {
            tocList.appendChild(item);
        }
        
        currentLevel = level;
    });
    
    return toc;
}

// 插入目录
function insertTOC(toc) {
    const content = document.querySelector('.page-content');
    const firstHeading = content.querySelector('h1, h2, h3, h4, h5, h6');
    
    if (firstHeading) {
        content.insertBefore(toc, firstHeading.nextSibling);
    }
}

// 返回顶部功能
function initScrollToTop() {
    // 创建返回顶部按钮
    const scrollBtn = document.createElement('button');
    scrollBtn.className = 'scroll-to-top';
    scrollBtn.innerHTML = '<i class="fas fa-chevron-up"></i>';
    scrollBtn.title = '返回顶部';
    scrollBtn.style.cssText = `
        position: fixed;
        bottom: 2rem;
        right: 2rem;
        width: 50px;
        height: 50px;
        background-color: var(--primary-color);
        color: white;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s ease;
        z-index: 1000;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;
    
    document.body.appendChild(scrollBtn);
    
    // 显示/隐藏按钮
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            scrollBtn.style.opacity = '1';
            scrollBtn.style.visibility = 'visible';
        } else {
            scrollBtn.style.opacity = '0';
            scrollBtn.style.visibility = 'hidden';
        }
    });
    
    // 点击返回顶部
    scrollBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// 主题切换功能
function initThemeToggle() {
    // 检测系统主题偏好
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
    
    // 创建主题切换按钮
    const themeBtn = document.createElement('button');
    themeBtn.className = 'theme-toggle';
    themeBtn.innerHTML = '<i class="fas fa-moon"></i>';
    themeBtn.title = '切换主题';
    themeBtn.style.cssText = `
        position: fixed;
        bottom: 2rem;
        right: 6rem;
        width: 50px;
        height: 50px;
        background-color: var(--background-color);
        color: var(--text-color);
        border: 1px solid var(--border-color);
        border-radius: 50%;
        cursor: pointer;
        transition: all 0.3s ease;
        z-index: 1000;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;
    
    document.body.appendChild(themeBtn);
    
    // 主题切换逻辑
    themeBtn.addEventListener('click', function() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        
        updateThemeIcon(themeBtn, newTheme);
    });
    
    // 初始化主题
    const savedTheme = localStorage.getItem('theme');
    const initialTheme = savedTheme || (prefersDark.matches ? 'dark' : 'light');
    
    document.documentElement.setAttribute('data-theme', initialTheme);
    updateThemeIcon(themeBtn, initialTheme);
    
    // 监听系统主题变化
    prefersDark.addEventListener('change', function(e) {
        if (!localStorage.getItem('theme')) {
            const newTheme = e.matches ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', newTheme);
            updateThemeIcon(themeBtn, newTheme);
        }
    });
}

// 更新主题图标
function updateThemeIcon(button, theme) {
    const icon = button.querySelector('i');
    if (theme === 'dark') {
        icon.className = 'fas fa-sun';
    } else {
        icon.className = 'fas fa-moon';
    }
}

// 工具函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 导出函数供其他脚本使用
window.TiGatewayDocs = {
    initNavigation,
    initSearch,
    initCodeCopy,
    initTableOfContents,
    initScrollToTop,
    initThemeToggle
};
