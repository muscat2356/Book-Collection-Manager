type StockBadgeProps = {
    stockCount: number
    maxStock?: number
}

function getStockStatus(stockCount: number): 'available' | 'low' | 'out' {
    if (stockCount === 0) return 'out'
    if (stockCount === 1) return 'low'
    return 'available'
}

function getStockLabel(stockCount: number): string {
    if (stockCount === 0) return '貸出できません'
    if (stockCount === 1) return '残りわずか'
    return '貸出可能'
}

export function StockBadge({ stockCount, maxStock = stockCount }: StockBadgeProps) {
    const status = getStockStatus(stockCount)
    const percent = maxStock > 0 ? Math.round((stockCount / maxStock) * 100) : 0

    return (
        <div className={`stock-indicator stock-indicator--${status}`}>
            <p className="stock-indicator__label">在庫</p>
            <p className="stock-indicator__count">
                <span className="stock-indicator__number">{stockCount}</span>
                <span className="stock-indicator__unit">冊</span>
                <span className="stock-indicator__max">/ {maxStock} 冊</span>
            </p>
            <div
                className="stock-indicator__bar"
                role="progressbar"
                aria-valuenow={stockCount}
                aria-valuemin={0}
                aria-valuemax={maxStock}
                aria-label={`在庫 ${stockCount} 冊`}
            >
                <div
                    className="stock-indicator__fill"
                    style={{ width: `${percent}%` }}
                />
            </div>
            <p className="stock-indicator__status">{getStockLabel(stockCount)}</p>
        </div>
    )
}
