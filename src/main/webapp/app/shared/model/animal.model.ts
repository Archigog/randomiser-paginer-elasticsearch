export interface IAnimal {
    id?: number;
    name?: string;
    promoted?: boolean;
}

export class Animal implements IAnimal {
    constructor(public id?: number, public name?: string, public promoted?: boolean) {
        this.promoted = this.promoted || false;
    }
}
