import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { AccountService } from 'app/core';

import { IAnimal } from 'app/shared/model/animal.model';
import { JhiAlertService, JhiEventManager, JhiParseLinks } from 'ng-jhipster';
import { Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { AnimalService } from './animal.service';

@Component({
    selector: 'jhi-animal',
    templateUrl: './animal.component.html',
    styleUrls: ['./animal.component.scss']
})
export class AnimalComponent implements OnInit, OnDestroy {
    currentAccount: any;
    animals: IAnimal[];
    error: any;
    success: any;
    eventSubscriber: Subscription;
    currentSearch: string;
    routeData: any;
    links: any;
    totalItems: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    private seed: string;
    private queryParamMapSubscriber: Subscription;

    constructor(
        protected animalService: AnimalService,
        protected parseLinks: JhiParseLinks,
        protected jhiAlertService: JhiAlertService,
        protected accountService: AccountService,
        protected activatedRoute: ActivatedRoute,
        protected router: Router,
        protected eventManager: JhiEventManager,
        private http: HttpClient
    ) {
        this.itemsPerPage = 9;
        this.routeData = this.activatedRoute.data.subscribe(data => {
            this.page = data.pagingParams.page;
            this.previousPage = data.pagingParams.page;
            this.reverse = data.pagingParams.ascending;
            this.predicate = data.pagingParams.predicate;
        });
        this.currentSearch =
            this.activatedRoute.snapshot && this.activatedRoute.snapshot.params['search']
                ? this.activatedRoute.snapshot.params['search']
                : '';
    }

    ngOnInit() {
        this.accountService.identity().then(account => {
            this.currentAccount = account;
        });
        this.registerChangeInAnimals();
        this.queryParamMapSubscriber = this.activatedRoute.queryParamMap.pipe(take(1)).subscribe((params: ParamMap) => {
            this.seed = params.get('seed') || Math.random().toString(36) + new Date().getTime().toString(36);
            this.loadAll();
        });
    }

    public loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    public clear() {
        this.page = 0;
        this.currentSearch = '';
        this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: {
                page: this.page,
                seed: this.seed
            }
        });
        this.loadAll();
    }

    public search(query) {
        if (!query) {
            return this.clear();
        }
        this.page = 0;
        this.currentSearch = query;
        this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: {
                search: this.currentSearch,
                page: this.page,
                seed: this.seed
            }
        });
        this.loadAll();
    }

    public registerChangeInAnimals() {
        this.eventSubscriber = this.eventManager.subscribe('animalListModification', () => this.loadAll());
    }

    public promoteAnimal(animal: IAnimal) {
        animal.promoted = !animal.promoted;
        this.animalService.update(animal).subscribe(() => {
            this.eventManager.broadcast('animalListModification');
        });
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.eventSubscriber);
    }

    protected paginateAnimals(data: IAnimal[], headers: HttpHeaders) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = parseInt(headers.get('X-Total-Count'), 10);
        this.animals = data;
    }

    protected onError(errorMessage: string) {
        this.jhiAlertService.error(errorMessage, null, null);
    }

    private loadAll() {
        if (this.currentSearch) {
            this.animalService
                .search({
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    query: this.currentSearch,
                    seed: this.seed
                })
                .subscribe(
                    (res: HttpResponse<IAnimal[]>) => this.paginateAnimals(res.body, res.headers),
                    (res: HttpErrorResponse) => this.onError(res.message)
                );
        } else {
            this.animalService
                .query({
                    page: this.page - 1,
                    size: this.itemsPerPage,
                    seed: this.seed
                })
                .subscribe(
                    (res: HttpResponse<IAnimal[]>) => this.paginateAnimals(res.body, res.headers),
                    (res: HttpErrorResponse) => this.onError(res.message)
                );
        }
    }

    private transition() {
        this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: {
                page: this.page,
                search: this.currentSearch,
                seed: this.seed
            }
        });
        this.loadAll();
    }
}
