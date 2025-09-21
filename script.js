document.addEventListener('DOMContentLoaded', () => {
    const table = document.getElementById('results-table');
    const tableHead = table.querySelector('thead');
    const tableBody = table.querySelector('tbody');
    const loadMoreBtn = document.getElementById('load-more-btn');
    const statusContainer = document.getElementById('status-container');

    let currentOffset = 0;
    let isLoading = false;
    const PAGE_SIZE = 200;

    async function fetchData() {
        if (isLoading) return;
        isLoading = true;
        loadMoreBtn.disabled = true;
        loadMoreBtn.textContent = '正在加载...';
        statusContainer.textContent = '';

        try {
            const response = await fetch(`/.netlify/functions/query?offset=${currentOffset}`);
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `服务器错误: ${response.status}`);
            }

            const data = await response.json();

            if (currentOffset === 0 && data.length > 0) {
                // 第一次加载，创建表头
                createTableHeader(Object.keys(data[0]));
            }

            if (data.length > 0) {
                appendTableRows(data);
                currentOffset += data.length;
            }

            if (data.length < PAGE_SIZE) {
                // 没有更多数据了
                loadMoreBtn.style.display = 'none'; // 直接隐藏按钮
                statusContainer.textContent = '已加载全部数据。';
            } else {
                loadMoreBtn.disabled = false;
                loadMoreBtn.textContent = '加载更多';
            }

        } catch (error) {
            console.error('获取数据失败:', error);
            statusContainer.textContent = `获取数据失败: ${error.message}`;
        } finally {
            isLoading = false;
            // 如果因为出错而停止，需要恢复按钮状态
            if (!loadMoreBtn.style.display) {
                loadMoreBtn.disabled = false;
                loadMoreBtn.textContent = '加载更多';
            }
        }
    }

    function createTableHeader(headers) {
        tableHead.innerHTML = ''; // 清空旧表头
        const headerRow = document.createElement('tr');
        headers.forEach(headerText => {
            const th = document.createElement('th');
            th.textContent = headerText;
            headerRow.appendChild(th);
        });
        tableHead.appendChild(headerRow);
    }

    function appendTableRows(rows) {
        rows.forEach(rowData => {
            const row = document.createElement('tr');
            for (const key in rowData) {
                const cell = document.createElement('td');
                cell.textContent = rowData[key];
                row.appendChild(cell);
            }
            tableBody.appendChild(row);
        });
    }

    loadMoreBtn.addEventListener('click', fetchData);

    // 初始加载
    fetchData();
});
