export interface IAnimal {
    id?: number;
    name?: string;
    promoted?: boolean;
    score?: number;
}

export class Animal implements IAnimal {
    constructor(public id?: number, public name?: string, public promoted?: boolean, public score?: number) {
        this.promoted = this.promoted || false;
    }
}
