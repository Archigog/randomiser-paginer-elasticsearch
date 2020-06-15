import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared';
import { IAnimal } from 'app/shared/model/animal.model';

type EntityResponseType = HttpResponse<IAnimal>;
type EntityArrayResponseType = HttpResponse<IAnimal[]>;

@Injectable({ providedIn: 'root' })
export class AnimalService {
    public resourceUrl = SERVER_API_URL + 'api/animals';
    public resourceSearchUrl = SERVER_API_URL + 'api/_search/animals';

    constructor(protected http: HttpClient) {}

    create(animal: IAnimal): Observable<EntityResponseType> {
        return this.http.post<IAnimal>(this.resourceUrl, animal, { observe: 'response' });
    }

    update(animal: IAnimal): Observable<EntityResponseType> {
        return this.http.put<IAnimal>(this.resourceUrl, animal, { observe: 'response' });
    }

    find(id: number): Observable<EntityResponseType> {
        return this.http.get<IAnimal>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    query(req?: any): Observable<EntityArrayResponseType> {
        const options = createRequestOption(req);
        return this.http.get<IAnimal[]>(this.resourceUrl, { params: options, observe: 'response' });
    }

    delete(id: number): Observable<HttpResponse<any>> {
        return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    search(req?: any): Observable<EntityArrayResponseType> {
        const options = createRequestOption(req);
        return this.http.get<IAnimal[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
    }

    getImage(animal: string): Observable<any> {
        return this.http.get(
            `http://en.wikipedia.org/w/api.php?action=query&titles=${animal
                .split(' ')
                .join('-')}&prop=pageimages&format=json&pithumbsize=100`
        );
    }
}
