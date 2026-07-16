import { createContext, useContext, useMemo, useState, type ReactNode } from "react"

export type CheckoutUser = {
    id: number
    displayName: string
}

export type SelectedBookCopy = {
    bookCopyId: number
    bookId: number
    title: string
}

type LoanContextValue = {
    user: CheckoutUser | null
    setUser: (user: CheckoutUser | null) => void
    selectedBookCopies: SelectedBookCopy[]
    addCopy: (copy: SelectedBookCopy) => void
    removeCopy: (bookCopyId: number) => void
    clearSelection: () => void
    clearAll: () => void
}

const LoanContext = createContext<LoanContextValue | null>(null)

export function LoanProvider({ children }: {children: ReactNode}) {
    const [user, setUser] = useState<CheckoutUser | null>(null)
    const [selectedBookCopies, setSelectedBookCopies] = useState<SelectedBookCopy[]>([])

    const value = useMemo<LoanContextValue>(() => {
        function addCopy(copy: SelectedBookCopy) {
            setSelectedBookCopies((prev) =>
                prev.some((c) => c.bookCopyId === copy.bookCopyId)
                    ? prev
                    : [...prev, copy]
            )
        }
        function removeCopy(bookCopyId: number) {
            setSelectedBookCopies((prev) =>
                prev.filter((c) => c.bookCopyId !== bookCopyId)
            )
        }
        function clearSelection() {
            setSelectedBookCopies([])
        }
        function clearAll() {
            setUser(null)
            setSelectedBookCopies([])
        }

        return {
            user,
            setUser,
            selectedBookCopies,
            addCopy,
            removeCopy,
            clearSelection,
            clearAll,
        }
    }, [user, selectedBookCopies])
    return (
    <LoanContext.Provider value={value}>{children}</LoanContext.Provider>
    )
}

export function useLoanCheckout(): LoanContextValue {
    const ctx = useContext(LoanContext)
    if(!ctx){
        throw new Error("useLoanCheckout は LoanProvider 内で使ってください")
    }
    return ctx
}
