type StockBadgeProps = {
    /** 貸出可能な冊数（AVAILABLE） */
    availableCount: number
    /** 所蔵総数（AVAILABLE + LOANED） */
    totalCount: number
}

function getStockStatus(availableCount: number): 'available' | 'low' | 'out' {
    if (availableCount === 0) return 'out'
    if (availableCount === 1) return 'low'
    return 'available'
}

function getStockLabel(availableCount: number): string {
    if (availableCount === 0) return '貸出できません'
    if (availableCount === 1) return '残りわずか'
    return '貸出可能'
}

export function StockBadge({ availableCount, totalCount }: StockBadgeProps) {
    const status = getStockStatus(availableCount)
    const percent =
        totalCount > 0 ? Math.round((availableCount / totalCount) * 100) : 0

    return (
        <div className={`stock-indicator stock-indicator--${status}`}>
            <p className="stock-indicator__label">所蔵状況</p>
            <p className="stock-indicator__count">
                <span className="stock-indicator__number">{availableCount}</span>
                <span className="stock-indicator__unit">冊</span>
                <span className="stock-indicator__max">/ 所蔵 {totalCount} 冊</span>
            </p>
            <div
                className="stock-indicator__bar"
                role="progressbar"
                aria-valuenow={availableCount}
                aria-valuemin={0}
                aria-valuemax={totalCount}
                aria-label={`貸出可能 ${availableCount} 冊 / 所蔵 ${totalCount} 冊`}
            >
                <div
                    className="stock-indicator__fill"
                    style={{ width: `${percent}%` }}
                />
            </div>
            <p className="stock-indicator__status">{getStockLabel(availableCount)}</p>
        </div>
    )
}
