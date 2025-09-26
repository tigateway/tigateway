// 目录生成和导航功能

class TableOfContents {
    constructor(options = {}) {
        this.options = {
            container: '.page-content',
            tocContainer: '.sidebar',
            headingSelector: 'h1, h2, h3, h4, h5, h6',
            minLevel: 1,
            maxLevel: 6,
            showLevel: 3,
            smoothScroll: true,
            highlightActive: true,
            ...options
        };
        
        this.headings = [];
        this.toc = null;
        this.activeHeading = null;
        
        this.init();
    }
    
    init() {
        this.extractHeadings();
        this.generateTOC();
        this.insertTOC();
        this.bindEvents();
    }
    
    // 提取标题
    extractHeadings() {
        const container = document.querySelector(this.options.container);
        if (!container) return;
        
        const headingElements = container.querySelectorAll(this.options.headingSelector);
        
        this.headings = Array.from(headingElements)
            .filter(heading => {
                const level = parseInt(heading.tagName.charAt(1));
                return level >= this.options.minLevel && level <= this.options.maxLevel;
            })
            .map(heading => {
                const level = parseInt(heading.tagName.charAt(1));
                const text = heading.textContent.trim();
                const id = this.generateHeadingId(text, heading);
                
                // 设置标题 ID
                if (!heading.id) {
                    heading.id = id;
                }
                
                return {
                    element: heading,
                    level: level,
                    text: text,
                    id: heading.id
                };
            });
    }
    
    // 生成标题 ID
    generateHeadingId(text, element) {
        // 如果已经有 ID，直接返回
        if (element.id) {
            return element.id;
        }
        
        // 生成基于文本的 ID
        let id = text
            .toLowerCase()
            .replace(/[^\w\s\u4e00-\u9fff-]/g, '') // 保留中文字符和连字符
            .replace(/\s+/g, '-')
            .trim();
        
        // 确保 ID 唯一
        let uniqueId = id;
        let counter = 1;
        while (document.getElementById(uniqueId)) {
            uniqueId = `${id}-${counter}`;
            counter++;
        }
        
        return uniqueId;
    }
    
    // 生成目录 HTML
    generateTOC() {
        if (this.headings.length === 0) return;
        
        const toc = document.createElement('div');
        toc.className = 'table-of-contents';
        
        const header = document.createElement('div');
        header.className = 'toc-header';
        header.innerHTML = `
            <h3>目录</h3>
            <button class="toc-toggle" title="切换目录显示">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6,9 12,15 18,9"></polyline>
                </svg>
            </button>
        `;
        
        const content = document.createElement('div');
        content.className = 'toc-content';
        
        const list = document.createElement('ul');
        list.className = 'toc-list';
        
        let currentLevel = 0;
        let listStack = [list];
        
        this.headings.forEach(heading => {
            // 创建列表项
            const item = document.createElement('li');
            item.className = `toc-item toc-level-${heading.level}`;
            
            const link = document.createElement('a');
            link.href = `#${heading.id}`;
            link.textContent = heading.text;
            link.className = 'toc-link';
            link.dataset.headingId = heading.id;
            
            item.appendChild(link);
            
            // 处理嵌套层级
            if (heading.level > currentLevel) {
                // 创建新的嵌套列表
                for (let i = currentLevel; i < heading.level; i++) {
                    const nestedList = document.createElement('ul');
                    nestedList.className = 'toc-list';
                    listStack[listStack.length - 1].appendChild(nestedList);
                    listStack.push(nestedList);
                }
            } else if (heading.level < currentLevel) {
                // 回到上级列表
                for (let i = currentLevel; i > heading.level; i--) {
                    listStack.pop();
                }
            }
            
            // 添加到当前列表
            listStack[listStack.length - 1].appendChild(item);
            currentLevel = heading.level;
        });
        
        content.appendChild(list);
        toc.appendChild(header);
        toc.appendChild(content);
        
        this.toc = toc;
    }
    
    // 插入目录
    insertTOC() {
        if (!this.toc) return;
        
        const container = document.querySelector(this.options.tocContainer);
        if (!container) {
            // 如果没有指定容器，插入到内容区域
            const content = document.querySelector(this.options.container);
            if (content) {
                const firstHeading = content.querySelector(this.options.headingSelector);
                if (firstHeading) {
                    content.insertBefore(this.toc, firstHeading.nextSibling);
                }
            }
            return;
        }
        
        // 插入到侧边栏
        const existingTOC = container.querySelector('.table-of-contents');
        if (existingTOC) {
            existingTOC.remove();
        }
        
        container.appendChild(this.toc);
    }
    
    // 绑定事件
    bindEvents() {
        if (!this.toc) return;
        
        // 目录切换
        const toggleBtn = this.toc.querySelector('.toc-toggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', () => {
                this.toggleTOC();
            });
        }
        
        // 目录链接点击
        const tocLinks = this.toc.querySelectorAll('.toc-link');
        tocLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.scrollToHeading(link.dataset.headingId);
            });
        });
        
        // 高亮当前标题
        if (this.options.highlightActive) {
            this.bindScrollEvents();
        }
    }
    
    // 切换目录显示
    toggleTOC() {
        const content = this.toc.querySelector('.toc-content');
        const toggleBtn = this.toc.querySelector('.toc-toggle');
        
        if (content.style.display === 'none') {
            content.style.display = 'block';
            toggleBtn.style.transform = 'rotate(0deg)';
        } else {
            content.style.display = 'none';
            toggleBtn.style.transform = 'rotate(180deg)';
        }
    }
    
    // 滚动到标题
    scrollToHeading(headingId) {
        const heading = document.getElementById(headingId);
        if (!heading) return;
        
        if (this.options.smoothScroll) {
            heading.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        } else {
            heading.scrollIntoView();
        }
        
        // 更新 URL
        if (history.pushState) {
            history.pushState(null, null, `#${headingId}`);
        }
    }
    
    // 绑定滚动事件
    bindScrollEvents() {
        let ticking = false;
        
        const updateActiveHeading = () => {
            const scrollTop = window.pageYOffset;
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight;
            
            // 找到当前可见的标题
            let currentHeading = null;
            
            for (let i = this.headings.length - 1; i >= 0; i--) {
                const heading = this.headings[i];
                const rect = heading.element.getBoundingClientRect();
                
                if (rect.top <= 100) { // 距离顶部 100px 以内
                    currentHeading = heading;
                    break;
                }
            }
            
            // 如果滚动到底部，选择最后一个标题
            if (scrollTop + windowHeight >= documentHeight - 100) {
                currentHeading = this.headings[this.headings.length - 1];
            }
            
            this.setActiveHeading(currentHeading);
            ticking = false;
        };
        
        const requestTick = () => {
            if (!ticking) {
                requestAnimationFrame(updateActiveHeading);
                ticking = true;
            }
        };
        
        window.addEventListener('scroll', requestTick, { passive: true });
    }
    
    // 设置活动标题
    setActiveHeading(heading) {
        if (this.activeHeading === heading) return;
        
        // 移除之前的活动状态
        if (this.activeHeading) {
            const prevLink = this.toc.querySelector(`[data-heading-id="${this.activeHeading.id}"]`);
            if (prevLink) {
                prevLink.classList.remove('active');
            }
        }
        
        // 设置新的活动状态
        this.activeHeading = heading;
        if (heading) {
            const link = this.toc.querySelector(`[data-heading-id="${heading.id}"]`);
            if (link) {
                link.classList.add('active');
                
                // 确保活动链接可见
                link.scrollIntoView({
                    behavior: 'smooth',
                    block: 'nearest'
                });
            }
        }
    }
    
    // 更新目录
    update() {
        this.extractHeadings();
        this.generateTOC();
        this.insertTOC();
        this.bindEvents();
    }
    
    // 获取目录数据
    getTOCData() {
        return this.headings.map(heading => ({
            level: heading.level,
            text: heading.text,
            id: heading.id
        }));
    }
    
    // 搜索标题
    searchHeadings(query) {
        const results = this.headings.filter(heading =>
            heading.text.toLowerCase().includes(query.toLowerCase())
        );
        
        return results.map(heading => ({
            level: heading.level,
            text: heading.text,
            id: heading.id
        }));
    }
}

// 目录样式
const tocStyles = `
<style>
.table-of-contents {
    background-color: var(--background-color);
    border: 1px solid var(--border-color);
    border-radius: var(--border-radius);
    margin-bottom: 1.5rem;
    overflow: hidden;
}

.toc-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem;
    background-color: var(--background-light);
    border-bottom: 1px solid var(--border-color);
}

.toc-header h3 {
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-color);
}

.toc-toggle {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 3px;
    transition: all 0.2s ease;
}

.toc-toggle:hover {
    background-color: var(--border-color);
}

.toc-toggle svg {
    transition: transform 0.2s ease;
}

.toc-content {
    padding: 0.5rem;
}

.toc-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.toc-item {
    margin: 0;
}

.toc-link {
    display: block;
    padding: 0.375rem 0.75rem;
    color: var(--text-light);
    text-decoration: none;
    border-radius: 3px;
    transition: all 0.2s ease;
    font-size: 0.875rem;
    line-height: 1.4;
}

.toc-link:hover {
    background-color: var(--background-light);
    color: var(--primary-color);
}

.toc-link.active {
    background-color: var(--primary-color);
    color: white;
    font-weight: 500;
}

.toc-level-1 .toc-link {
    padding-left: 0.75rem;
    font-weight: 600;
}

.toc-level-2 .toc-link {
    padding-left: 1.5rem;
}

.toc-level-3 .toc-link {
    padding-left: 2.25rem;
}

.toc-level-4 .toc-link {
    padding-left: 3rem;
}

.toc-level-5 .toc-link {
    padding-left: 3.75rem;
}

.toc-level-6 .toc-link {
    padding-left: 4.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .table-of-contents {
        margin-bottom: 1rem;
    }
    
    .toc-header {
        padding: 0.75rem;
    }
    
    .toc-content {
        padding: 0.25rem;
    }
    
    .toc-link {
        padding: 0.25rem 0.5rem;
        font-size: 0.8rem;
    }
    
    .toc-level-1 .toc-link {
        padding-left: 0.5rem;
    }
    
    .toc-level-2 .toc-link {
        padding-left: 1rem;
    }
    
    .toc-level-3 .toc-link {
        padding-left: 1.5rem;
    }
    
    .toc-level-4 .toc-link {
        padding-left: 2rem;
    }
    
    .toc-level-5 .toc-link {
        padding-left: 2.5rem;
    }
    
    .toc-level-6 .toc-link {
        padding-left: 3rem;
    }
}
</style>
`;

// 插入样式
document.head.insertAdjacentHTML('beforeend', tocStyles);

// 初始化目录功能
document.addEventListener('DOMContentLoaded', function() {
    const toc = new TableOfContents({
        container: '.page-content',
        tocContainer: '.sidebar',
        headingSelector: 'h1, h2, h3, h4, h5, h6',
        minLevel: 2,
        maxLevel: 4,
        showLevel: 3,
        smoothScroll: true,
        highlightActive: true
    });
    
    // 暴露到全局
    window.TiGatewayTOC = toc;
});
