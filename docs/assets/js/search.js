// TiGateway 文档搜索功能

class TiGatewaySearch {
    constructor() {
        this.searchIndex = [];
        this.searchResults = [];
        this.isInitialized = false;
        this.init();
    }
    
    async init() {
        await this.buildSearchIndex();
        this.isInitialized = true;
    }
    
    // 构建搜索索引
    async buildSearchIndex() {
        const pages = await this.fetchAllPages();
        
        pages.forEach(page => {
            const content = this.extractTextContent(page.content);
            const words = this.tokenize(content);
            
            this.searchIndex.push({
                title: page.title,
                url: page.url,
                content: content,
                words: words,
                type: page.type || 'page'
            });
        });
    }
    
    // 获取所有页面内容
    async fetchAllPages() {
        // 这里需要根据实际的 Jekyll 站点结构来获取页面
        // 可以通过 AJAX 请求获取所有页面的内容
        const pages = [];
        
        // 示例页面数据
        const pageUrls = [
            '/',
            '/book/01-introduction.md',
            '/book/02-quick-start.md',
            '/book/03-installation.md',
            '/architecture/system-architecture.md',
            '/development/setup.md',
            '/api/rest-api.md',
            '/examples/quick-start.md'
        ];
        
        for (const url of pageUrls) {
            try {
                const response = await fetch(url);
                const html = await response.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                const title = doc.querySelector('h1')?.textContent || 'Untitled';
                const content = doc.querySelector('.page-content')?.textContent || '';
                
                pages.push({
                    title: title,
                    url: url,
                    content: content,
                    type: this.getPageType(url)
                });
            } catch (error) {
                console.warn(`Failed to fetch page: ${url}`, error);
            }
        }
        
        return pages;
    }
    
    // 获取页面类型
    getPageType(url) {
        if (url.includes('/book/')) return 'book';
        if (url.includes('/architecture/')) return 'architecture';
        if (url.includes('/development/')) return 'development';
        if (url.includes('/api/')) return 'api';
        if (url.includes('/deployment/')) return 'deployment';
        if (url.includes('/examples/')) return 'examples';
        if (url.includes('/configuration/')) return 'configuration';
        return 'page';
    }
    
    // 提取文本内容
    extractTextContent(html) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        
        // 移除脚本和样式标签
        const scripts = doc.querySelectorAll('script, style');
        scripts.forEach(script => script.remove());
        
        return doc.body.textContent || '';
    }
    
    // 分词处理
    tokenize(text) {
        return text
            .toLowerCase()
            .replace(/[^\w\s\u4e00-\u9fff]/g, ' ') // 保留中文字符
            .split(/\s+/)
            .filter(word => word.length > 1)
            .filter(word => !this.isStopWord(word));
    }
    
    // 停用词过滤
    isStopWord(word) {
        const stopWords = [
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for', 'of', 'with', 'by',
            'is', 'are', 'was', 'were', 'be', 'been', 'being', 'have', 'has', 'had', 'do', 'does', 'did',
            'will', 'would', 'could', 'should', 'may', 'might', 'must', 'can', 'this', 'that', 'these', 'those',
            '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个', '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好', '自己', '这'
        ];
        return stopWords.includes(word);
    }
    
    // 搜索功能
    search(query, options = {}) {
        if (!this.isInitialized) {
            return [];
        }
        
        const {
            limit = 10,
            type = null,
            fuzzy = true
        } = options;
        
        const queryWords = this.tokenize(query);
        if (queryWords.length === 0) {
            return [];
        }
        
        const results = this.searchIndex
            .filter(item => !type || item.type === type)
            .map(item => {
                const score = this.calculateScore(item, queryWords, fuzzy);
                return {
                    ...item,
                    score: score
                };
            })
            .filter(item => item.score > 0)
            .sort((a, b) => b.score - a.score)
            .slice(0, limit);
        
        this.searchResults = results;
        return results;
    }
    
    // 计算相关性得分
    calculateScore(item, queryWords, fuzzy) {
        let score = 0;
        
        // 标题匹配得分更高
        const titleWords = this.tokenize(item.title);
        queryWords.forEach(queryWord => {
            titleWords.forEach(titleWord => {
                if (titleWord.includes(queryWord) || queryWord.includes(titleWord)) {
                    score += 10;
                }
            });
        });
        
        // 内容匹配
        queryWords.forEach(queryWord => {
            item.words.forEach(word => {
                if (word.includes(queryWord) || queryWord.includes(word)) {
                    score += 1;
                }
            });
        });
        
        // 模糊匹配
        if (fuzzy) {
            queryWords.forEach(queryWord => {
                item.words.forEach(word => {
                    const distance = this.levenshteinDistance(queryWord, word);
                    const maxLength = Math.max(queryWord.length, word.length);
                    const similarity = 1 - (distance / maxLength);
                    
                    if (similarity > 0.6) {
                        score += similarity * 0.5;
                    }
                });
            });
        }
        
        return score;
    }
    
    // 计算编辑距离
    levenshteinDistance(str1, str2) {
        const matrix = [];
        
        for (let i = 0; i <= str2.length; i++) {
            matrix[i] = [i];
        }
        
        for (let j = 0; j <= str1.length; j++) {
            matrix[0][j] = j;
        }
        
        for (let i = 1; i <= str2.length; i++) {
            for (let j = 1; j <= str1.length; j++) {
                if (str2.charAt(i - 1) === str1.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = Math.min(
                        matrix[i - 1][j - 1] + 1,
                        matrix[i][j - 1] + 1,
                        matrix[i - 1][j] + 1
                    );
                }
            }
        }
        
        return matrix[str2.length][str1.length];
    }
    
    // 高亮搜索结果
    highlightSearchResult(text, query) {
        const queryWords = this.tokenize(query);
        let highlightedText = text;
        
        queryWords.forEach(word => {
            const regex = new RegExp(`(${word})`, 'gi');
            highlightedText = highlightedText.replace(regex, '<mark>$1</mark>');
        });
        
        return highlightedText;
    }
    
    // 获取搜索建议
    getSuggestions(query, limit = 5) {
        if (!this.isInitialized || query.length < 2) {
            return [];
        }
        
        const suggestions = new Set();
        const queryLower = query.toLowerCase();
        
        this.searchIndex.forEach(item => {
            // 标题建议
            if (item.title.toLowerCase().includes(queryLower)) {
                suggestions.add(item.title);
            }
            
            // 内容建议
            item.words.forEach(word => {
                if (word.startsWith(queryLower) && word.length > query.length) {
                    suggestions.add(word);
                }
            });
        });
        
        return Array.from(suggestions).slice(0, limit);
    }
    
    // 获取热门搜索
    getPopularSearches() {
        return [
            '快速开始',
            '安装部署',
            '配置管理',
            'API 文档',
            '故障排除',
            '性能调优',
            '安全配置',
            '监控指标'
        ];
    }
}

// 搜索 UI 组件
class SearchUI {
    constructor(searchEngine) {
        this.searchEngine = searchEngine;
        this.searchInput = null;
        this.resultsContainer = null;
        this.isVisible = false;
        this.currentQuery = '';
        
        this.init();
    }
    
    init() {
        this.createSearchUI();
        this.bindEvents();
    }
    
    // 创建搜索界面
    createSearchUI() {
        const searchBox = document.querySelector('.search-box');
        if (!searchBox) return;
        
        // 创建搜索结果容器
        this.resultsContainer = document.createElement('div');
        this.resultsContainer.className = 'search-results-container';
        this.resultsContainer.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            max-height: 400px;
            overflow-y: auto;
            z-index: 1000;
            display: none;
        `;
        
        searchBox.style.position = 'relative';
        searchBox.appendChild(this.resultsContainer);
        
        this.searchInput = searchBox.querySelector('input');
    }
    
    // 绑定事件
    bindEvents() {
        if (!this.searchInput) return;
        
        // 输入事件
        this.searchInput.addEventListener('input', (e) => {
            this.handleSearch(e.target.value);
        });
        
        // 焦点事件
        this.searchInput.addEventListener('focus', () => {
            if (this.currentQuery) {
                this.showResults();
            }
        });
        
        // 点击外部隐藏
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.search-box')) {
                this.hideResults();
            }
        });
        
        // 键盘事件
        this.searchInput.addEventListener('keydown', (e) => {
            this.handleKeydown(e);
        });
    }
    
    // 处理搜索
    async handleSearch(query) {
        this.currentQuery = query.trim();
        
        if (this.currentQuery.length < 2) {
            this.hideResults();
            return;
        }
        
        const results = this.searchEngine.search(this.currentQuery, { limit: 8 });
        this.displayResults(results, this.currentQuery);
    }
    
    // 显示搜索结果
    displayResults(results, query) {
        if (results.length === 0) {
            this.showNoResults(query);
            return;
        }
        
        const html = `
            <div class="search-results-header">
                <strong>找到 ${results.length} 个结果</strong>
            </div>
            <div class="search-results-list">
                ${results.map(result => `
                    <div class="search-result-item" data-url="${result.url}">
                        <div class="search-result-title">
                            ${this.searchEngine.highlightSearchResult(result.title, query)}
                        </div>
                        <div class="search-result-url">${result.url}</div>
                        <div class="search-result-type">${result.type}</div>
                    </div>
                `).join('')}
            </div>
        `;
        
        this.resultsContainer.innerHTML = html;
        this.bindResultEvents();
        this.showResults();
    }
    
    // 显示无结果
    showNoResults(query) {
        const suggestions = this.searchEngine.getSuggestions(query);
        const popular = this.searchEngine.getPopularSearches();
        
        let html = `
            <div class="search-no-results">
                <div class="no-results-message">
                    <strong>没有找到相关结果</strong>
                    <p>尝试使用其他关键词搜索</p>
                </div>
        `;
        
        if (suggestions.length > 0) {
            html += `
                <div class="search-suggestions">
                    <strong>搜索建议:</strong>
                    <div class="suggestion-tags">
                        ${suggestions.map(suggestion => `
                            <span class="suggestion-tag" data-query="${suggestion}">${suggestion}</span>
                        `).join('')}
                    </div>
                </div>
            `;
        }
        
        html += `
                <div class="popular-searches">
                    <strong>热门搜索:</strong>
                    <div class="popular-tags">
                        ${popular.map(term => `
                            <span class="popular-tag" data-query="${term}">${term}</span>
                        `).join('')}
                    </div>
                </div>
            </div>
        `;
        
        this.resultsContainer.innerHTML = html;
        this.bindSuggestionEvents();
        this.showResults();
    }
    
    // 绑定结果事件
    bindResultEvents() {
        const resultItems = this.resultsContainer.querySelectorAll('.search-result-item');
        resultItems.forEach(item => {
            item.addEventListener('click', () => {
                const url = item.dataset.url;
                window.location.href = url;
            });
        });
    }
    
    // 绑定建议事件
    bindSuggestionEvents() {
        const suggestionTags = this.resultsContainer.querySelectorAll('.suggestion-tag, .popular-tag');
        suggestionTags.forEach(tag => {
            tag.addEventListener('click', () => {
                const query = tag.dataset.query;
                this.searchInput.value = query;
                this.handleSearch(query);
            });
        });
    }
    
    // 处理键盘事件
    handleKeydown(e) {
        const results = this.resultsContainer.querySelectorAll('.search-result-item');
        const currentActive = this.resultsContainer.querySelector('.search-result-item.active');
        let activeIndex = -1;
        
        if (currentActive) {
            activeIndex = Array.from(results).indexOf(currentActive);
        }
        
        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                activeIndex = Math.min(activeIndex + 1, results.length - 1);
                this.setActiveResult(results[activeIndex]);
                break;
            case 'ArrowUp':
                e.preventDefault();
                activeIndex = Math.max(activeIndex - 1, -1);
                if (activeIndex === -1) {
                    this.clearActiveResult();
                } else {
                    this.setActiveResult(results[activeIndex]);
                }
                break;
            case 'Enter':
                e.preventDefault();
                if (currentActive) {
                    const url = currentActive.dataset.url;
                    window.location.href = url;
                }
                break;
            case 'Escape':
                this.hideResults();
                this.searchInput.blur();
                break;
        }
    }
    
    // 设置活动结果
    setActiveResult(result) {
        this.clearActiveResult();
        if (result) {
            result.classList.add('active');
            result.scrollIntoView({ block: 'nearest' });
        }
    }
    
    // 清除活动结果
    clearActiveResult() {
        const active = this.resultsContainer.querySelector('.search-result-item.active');
        if (active) {
            active.classList.remove('active');
        }
    }
    
    // 显示结果
    showResults() {
        this.resultsContainer.style.display = 'block';
        this.isVisible = true;
    }
    
    // 隐藏结果
    hideResults() {
        this.resultsContainer.style.display = 'none';
        this.isVisible = false;
    }
}

// 初始化搜索功能
document.addEventListener('DOMContentLoaded', async function() {
    const searchEngine = new TiGatewaySearch();
    const searchUI = new SearchUI(searchEngine);
    
    // 将搜索实例暴露到全局
    window.TiGatewaySearch = searchEngine;
    window.TiGatewaySearchUI = searchUI;
});
