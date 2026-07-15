export type LoanStatus = 'BORROWED' | 'RETURNED'


export type Loan = {
    id : number;
    bookId : number;
    userId : number;
    borrowedAt : string;
    returnedAt : string | null;
    status : LoanStatus;
}
