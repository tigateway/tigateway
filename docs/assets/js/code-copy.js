// 代码复制功能

class CodeCopy {
    constructor() {
        this.copyButtons = new Map();
        this.init();
    }
    
    init() {
        this.addCopyButtons();
        this.bindEvents();
    }
    
    // 添加复制按钮
    addCopyButtons() {
        const codeBlocks = document.querySelectorAll('pre code');
        
        codeBlocks.forEach((codeBlock, index) => {
            const pre = codeBlock.parentElement;
            
            // 跳过已经有复制按钮的代码块
            if (pre.querySelector('.code-copy-btn')) {
                return;
            }
            
            // 创建复制按钮
            const copyBtn = this.createCopyButton(index);
            
            // 设置代码块样式
            pre.style.position = 'relative';
            pre.style.paddingTop = '2.5rem';
            
            // 添加按钮到代码块
            pre.appendChild(copyBtn);
            
            // 存储按钮引用
            this.copyButtons.set(index, copyBtn);
        });
    }
    
    // 创建复制按钮
    createCopyButton(index) {
        const button = document.createElement('button');
        button.className = 'code-copy-btn';
        button.dataset.index = index;
        button.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
            <span class="copy-text">复制</span>
        `;
        
        // 设置按钮样式
        button.style.cssText = `
            position: absolute;
            top: 0.5rem;
            right: 0.5rem;
            display: flex;
            align-items: center;
            gap: 0.25rem;
            padding: 0.375rem 0.75rem;
            background-color: rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(0, 0, 0, 0.1);
            border-radius: 4px;
            color: #666;
            font-size: 0.75rem;
            cursor: pointer;
            opacity: 0;
            transition: all 0.2s ease;
            z-index: 10;
        `;
        
        return button;
    }
    
    // 绑定事件
    bindEvents() {
        // 代码块悬停显示复制按钮
        document.addEventListener('mouseover', (e) => {
            const pre = e.target.closest('pre');
            if (pre) {
                const copyBtn = pre.querySelector('.code-copy-btn');
                if (copyBtn) {
                    copyBtn.style.opacity = '1';
                }
            }
        });
        
        // 代码块离开隐藏复制按钮
        document.addEventListener('mouseout', (e) => {
            const pre = e.target.closest('pre');
            if (pre) {
                const copyBtn = pre.querySelector('.code-copy-btn');
                if (copyBtn && !copyBtn.matches(':hover')) {
                    copyBtn.style.opacity = '0';
                }
            }
        });
        
        // 复制按钮点击事件
        document.addEventListener('click', (e) => {
            if (e.target.closest('.code-copy-btn')) {
                e.preventDefault();
                e.stopPropagation();
                
                const button = e.target.closest('.code-copy-btn');
                const index = parseInt(button.dataset.index);
                this.copyCode(index);
            }
        });
        
        // 键盘快捷键 (Ctrl/Cmd + C)
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'c') {
                const activeElement = document.activeElement;
                const pre = activeElement.closest('pre');
                
                if (pre && pre.querySelector('code')) {
                    e.preventDefault();
                    const copyBtn = pre.querySelector('.code-copy-btn');
                    if (copyBtn) {
                        const index = parseInt(copyBtn.dataset.index);
                        this.copyCode(index);
                    }
                }
            }
        });
    }
    
    // 复制代码
    async copyCode(index) {
        const codeBlocks = document.querySelectorAll('pre code');
        const codeBlock = codeBlocks[index];
        
        if (!codeBlock) {
            return;
        }
        
        const text = this.getCodeText(codeBlock);
        const button = this.copyButtons.get(index);
        
        try {
            await this.copyToClipboard(text);
            this.showCopySuccess(button);
        } catch (error) {
            console.error('复制失败:', error);
            this.showCopyError(button);
        }
    }
    
    // 获取代码文本
    getCodeText(codeBlock) {
        // 处理行号
        const lines = codeBlock.querySelectorAll('.line-number');
        if (lines.length > 0) {
            // 移除行号
            const clone = codeBlock.cloneNode(true);
            const lineNumbers = clone.querySelectorAll('.line-number');
            lineNumbers.forEach(line => line.remove());
            return clone.textContent;
        }
        
        return codeBlock.textContent;
    }
    
    // 复制到剪贴板
    async copyToClipboard(text) {
        if (navigator.clipboard && window.isSecureContext) {
            // 使用现代 Clipboard API
            await navigator.clipboard.writeText(text);
        } else {
            // 降级到传统方法
            this.fallbackCopyText(text);
        }
    }
    
    // 备用复制方法
    fallbackCopyText(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        textArea.style.opacity = '0';
        textArea.style.pointerEvents = 'none';
        textArea.setAttribute('readonly', '');
        
        document.body.appendChild(textArea);
        
        try {
            textArea.focus();
            textArea.select();
            textArea.setSelectionRange(0, 99999); // 移动端兼容
            
            const successful = document.execCommand('copy');
            if (!successful) {
                throw new Error('execCommand failed');
            }
        } finally {
            document.body.removeChild(textArea);
        }
    }
    
    // 显示复制成功
    showCopySuccess(button) {
        const originalHTML = button.innerHTML;
        const copyText = button.querySelector('.copy-text');
        
        // 更新按钮状态
        button.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20,6 9,17 4,12"></polyline>
            </svg>
            <span class="copy-text">已复制!</span>
        `;
        
        button.style.backgroundColor = '#28a745';
        button.style.color = 'white';
        button.style.borderColor = '#28a745';
        
        // 恢复原始状态
        setTimeout(() => {
            button.innerHTML = originalHTML;
            button.style.backgroundColor = '';
            button.style.color = '';
            button.style.borderColor = '';
        }, 2000);
    }
    
    // 显示复制错误
    showCopyError(button) {
        const originalHTML = button.innerHTML;
        
        button.innerHTML = `
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
            <span class="copy-text">复制失败</span>
        `;
        
        button.style.backgroundColor = '#dc3545';
        button.style.color = 'white';
        button.style.borderColor = '#dc3545';
        
        // 恢复原始状态
        setTimeout(() => {
            button.innerHTML = originalHTML;
            button.style.backgroundColor = '';
            button.style.color = '';
            button.style.borderColor = '';
        }, 2000);
    }
    
    // 添加行号支持
    addLineNumbers() {
        const codeBlocks = document.querySelectorAll('pre code');
        
        codeBlocks.forEach(codeBlock => {
            if (codeBlock.querySelector('.line-number')) {
                return; // 已经有行号了
            }
            
            const lines = codeBlock.textContent.split('\n');
            if (lines.length < 2) {
                return; // 单行代码不需要行号
            }
            
            const numberedLines = lines.map((line, index) => {
                const lineNumber = index + 1;
                return `<span class="line-number" data-line="${lineNumber}">${lineNumber}</span>${line}`;
            }).join('\n');
            
            codeBlock.innerHTML = numberedLines;
        });
    }
    
    // 支持语言标识
    addLanguageLabels() {
        const codeBlocks = document.querySelectorAll('pre[class*="language-"]');
        
        codeBlocks.forEach(pre => {
            if (pre.querySelector('.language-label')) {
                return; // 已经有语言标签了
            }
            
            const className = pre.className;
            const languageMatch = className.match(/language-(\w+)/);
            
            if (languageMatch) {
                const language = languageMatch[1];
                const label = this.getLanguageLabel(language);
                
                const labelElement = document.createElement('div');
                labelElement.className = 'language-label';
                labelElement.textContent = label;
                labelElement.style.cssText = `
                    position: absolute;
                    top: 0.5rem;
                    left: 0.5rem;
                    padding: 0.25rem 0.5rem;
                    background-color: rgba(0, 0, 0, 0.1);
                    border-radius: 3px;
                    font-size: 0.75rem;
                    color: #666;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                `;
                
                pre.style.position = 'relative';
                pre.appendChild(labelElement);
            }
        });
    }
    
    // 获取语言标签
    getLanguageLabel(language) {
        const labels = {
            'javascript': 'JS',
            'typescript': 'TS',
            'java': 'Java',
            'python': 'Python',
            'yaml': 'YAML',
            'json': 'JSON',
            'xml': 'XML',
            'html': 'HTML',
            'css': 'CSS',
            'bash': 'Bash',
            'shell': 'Shell',
            'sql': 'SQL',
            'dockerfile': 'Docker',
            'markdown': 'MD'
        };
        
        return labels[language] || language.toUpperCase();
    }
    
    // 支持代码折叠
    addCodeFolding() {
        const codeBlocks = document.querySelectorAll('pre code');
        
        codeBlocks.forEach((codeBlock, index) => {
            const lines = codeBlock.textContent.split('\n');
            if (lines.length < 10) {
                return; // 短代码不需要折叠
            }
            
            const pre = codeBlock.parentElement;
            const foldBtn = document.createElement('button');
            foldBtn.className = 'code-fold-btn';
            foldBtn.innerHTML = '折叠';
            foldBtn.style.cssText = `
                position: absolute;
                top: 0.5rem;
                right: 4rem;
                padding: 0.25rem 0.5rem;
                background-color: rgba(0, 0, 0, 0.1);
                border: 1px solid rgba(0, 0, 0, 0.1);
                border-radius: 3px;
                font-size: 0.75rem;
                cursor: pointer;
                opacity: 0;
                transition: opacity 0.2s ease;
            `;
            
            let isFolded = false;
            
            foldBtn.addEventListener('click', () => {
                if (isFolded) {
                    codeBlock.style.maxHeight = 'none';
                    foldBtn.textContent = '折叠';
                    isFolded = false;
                } else {
                    codeBlock.style.maxHeight = '200px';
                    foldBtn.textContent = '展开';
                    isFolded = true;
                }
            });
            
            pre.addEventListener('mouseenter', () => {
                foldBtn.style.opacity = '1';
            });
            
            pre.addEventListener('mouseleave', () => {
                foldBtn.style.opacity = '0';
            });
            
            pre.appendChild(foldBtn);
        });
    }
}

// 初始化代码复制功能
document.addEventListener('DOMContentLoaded', function() {
    const codeCopy = new CodeCopy();
    
    // 可选功能
    // codeCopy.addLineNumbers();
    // codeCopy.addLanguageLabels();
    // codeCopy.addCodeFolding();
    
    // 暴露到全局
    window.TiGatewayCodeCopy = codeCopy;
});
