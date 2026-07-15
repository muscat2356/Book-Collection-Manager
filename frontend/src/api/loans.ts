
import { mockBooks } from "../data/mockBooks"
import { mockLoans } from "../data/mockLoans"
import { mockUsers } from "../data/mockUsers"
import type { Loan } from "../types/Loan"

export type ActiveLoan = {
    id: number
    book: { id: number, title: string, author: string }
    user: { id: number, displayName: string }
    borrowedAt: string
    returnedAt: null
    status: 'BORROWED'
}

export async function createLoan(bookId: number, userId: number): Promise<Loan>{ 
    await new Promise((r) => setTimeout(r, 500))

    const book = mockBooks.find((b) => b.id === bookId)
    if(!book) throw new Error('書籍が見つかりません')

    const user = mockUsers.find((u) => u.id === userId)
    if(!user) throw new Error('利用者が見つかりません')

    if(book.totalCount <= 0) throw new Error('在庫がありません')

        book.totalCount -= 1

    const newLoan: Loan = {
        id: Number(mockLoans.length + 1),
        bookId: bookId,
        userId: userId,
        borrowedAt: Date().toString(),
        returnedAt: null,
        status: 'BORROWED'
    }

    mockLoans.push(newLoan)
    return newLoan
}

export async function returnLoans(loanId:number) {
    const loan = mockLoans.find((l) => l.id === loanId)
    if(!loan) throw new Error('貸し出し履歴がありません');
    
    const book = mockBooks.find((b) => b.id === loan.bookId)
    if(!book) throw new Error('対象の本がありません');
    
    loan.status = 'RETURNED';
    loan.returnedAt = Date().toString();
    book.totalCount += 1;
}

export async function fetchActiveLoans():Promise<ActiveLoan[]> {
    await new Promise((r) => setTimeout(r, 500))

    return mockLoans
    .filter((loan) => loan.status === 'BORROWED')
    .map((loan) => {
        const book = mockBooks.find((b) => b.id === loan.bookId)
        const user = mockUsers.find((u) => u.id === loan.userId)

        if(!book || !user) {
            throw new Error('貸出データが見つかりません')
        }

        return{
            id: loan.id,
            book: {
                id: book.id,
                title: book.title,
                author: book.author,
            },
            user: {
                id: user.id,
                displayName: user.displayName,
            },
            borrowedAt: loan.borrowedAt,
            returnedAt: null,
            status: 'BORROWED' as const,
        }
    })

}
