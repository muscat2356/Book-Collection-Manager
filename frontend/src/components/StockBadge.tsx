type StockBadgeProps = {
    availableCount: number
    totalCount: number
}

function getStockStatus(availableCount: number): 'available' | 'low' | 'out' {
    if (availableCount === 0) return 'out'
    if (availableCount === 1) return 'low'
    return 'available'
}

export function StockBadge({ availableCount, totalCount }: StockBadgeProps) {
    const status = getStockStatus(availableCount)

    return (
        <div className={`stock-indicator stock-indicator--${status}`}>
            <p className="stock-indicator__label">所蔵</p>
            <p className="stock-indicator__count">
                貸出可能 {availableCount} / 所蔵 {totalCount}
            </p>
        </div>
    )
}
