import { mockBooks } from "../data/mockBooks";
import type { Book } from "../types/Book";

export type CreateBookRequest = {
    title: string
    author: string
    isbn: string
    stockCount: number
}

export type UpdateBookRequest = {
    title: string
    author: string
    isbn: string
    stockCount: number
}


export async function fetchBooks(): Promise<Book[]> {
    // API 呼び出しの代わりに、モックデータを返す
    await new Promise((resolve) => setTimeout(resolve, 1000))
    return mockBooks;   

    // API 呼び出しの実装
    // const response = await apiClient.get('/books');
    // return response.data;
}

export async function fetchBookById(id:string): Promise<Book | null> {
    const book = mockBooks.find((book) => book.id === id);
    return book ?? null

    // API 呼び出しの実装
    // const response = await apiClient.get(`/books/${id}`);
    // return response.data;
}

export async function createBook(data: CreateBookRequest): Promise<Book> {
    await new Promise((r) => setTimeout(r, 500))

    const newBook:Book = {
        id: String(mockBooks.length + 1),
        title: data.title,
        author: data.author,
        isbn: data.isbn,
        stockCount: data.stockCount
    }

    mockBooks.push(newBook)
    return newBook
}

export async function updateBook(id: string, data: UpdateBookRequest): Promise<Book> {
    await new Promise((r) => setTimeout(r, 500))

    const index = mockBooks.findIndex((u) => u.id === id)
    if(index === -1){
        throw new Error('利用者が見つかりません')
    }

    mockBooks[index] = {
        ...mockBooks[index],
        title: data.title,
        author: data.author,
        isbn: data.isbn,
        stockCount: data.stockCount
    }

    return mockBooks[index]
    
}
